package gajeman.jagalchi.jagalchiserver.application.snapshot.service;

import gajeman.jagalchi.jagalchiserver.application.snapshot.usecase.GetSnapshotUseCase;
import gajeman.jagalchi.jagalchiserver.domain.event.Snapshot;
import gajeman.jagalchi.jagalchiserver.domain.node.RoadmapEdge;
import gajeman.jagalchi.jagalchiserver.domain.node.RoadmapNode;
import gajeman.jagalchi.jagalchiserver.domain.node.RoadmapSection;
import gajeman.jagalchi.jagalchiserver.infrastructure.persistence.RoadmapEdgeRepository;
import gajeman.jagalchi.jagalchiserver.infrastructure.persistence.RoadmapNodeRepository;
import gajeman.jagalchi.jagalchiserver.infrastructure.persistence.RoadmapSectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * SNAPSHOT 생성 유즈케이스 구현체
 * logic.md: 초기 연결 시 전체 상태를 제공하는 SNAPSHOT
 *
 * convention.md: Factory Method는 from으로 통일
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetSnapshotUseCaseImpl implements GetSnapshotUseCase {

    private final RoadmapNodeRepository roadmapNodeRepository;
    private final RoadmapEdgeRepository roadmapEdgeRepository;
    private final RoadmapSectionRepository roadmapSectionRepository;

    /**
     * 로드맵의 전체 상태 조회
     *
     * @param roadmapId 로드맵 ID (String)
     * @return SNAPSHOT (nodes, edges, sections 포함)
     */
    @Override
    @Transactional(readOnly = true)
    public Snapshot getSnapshot(
            String roadmapId
    ) {
        log.info("Creating SNAPSHOT for roadmapId={}", roadmapId);

        // 1. roadmapId를 Long으로 변환 (unitId)
        Long unitId = Long.parseLong(roadmapId);

        // 2. 노드 조회
        List<RoadmapNode> nodes = roadmapNodeRepository.findByUnitId(unitId);
        List<Map<String, Object>> nodeDtos = nodes.stream()
                .map(RoadmapNode::toDto)
                .toList();

        // 3. 엣지(간선) 조회
        List<RoadmapEdge> edges = roadmapEdgeRepository.findByUnitId(unitId);
        List<Map<String, Object>> edgeDtos = edges.stream()
                .map(RoadmapEdge::toDto)
                .toList();

        // 4. 섹션 조회
        List<RoadmapSection> sections = roadmapSectionRepository.findByUnitId(unitId);
        List<Map<String, Object>> sectionDtos = sections.stream()
                .map(RoadmapSection::toDto)
                .toList();

        // 5. 고아 노드 ID 조회 (간선이 없는 노드들)
        List<RoadmapEdge> allEdges = roadmapEdgeRepository.findByUnitId(unitId);
        Set<Long> connectedNodeIds = allEdges.stream()
                .flatMap(edge -> Stream.of(edge.getFromNodeId(), edge.getToNodeId()))
                .collect(Collectors.toSet());

        List<Long> orphanNodeIds = nodes.stream()
                .map(RoadmapNode::getId)
                .filter(nodeId -> !connectedNodeIds.contains(nodeId))
                .toList();

        // 6. 버전 생성 (현재 시스템 시간 사용)
        Long version = System.currentTimeMillis();

        log.info("SNAPSHOT created: roadmapId={}, version={}, nodes={}, edges={}, sections={}, orphanNodes={}",
                roadmapId, version, nodeDtos.size(), edgeDtos.size(), sectionDtos.size(), orphanNodeIds.size());

        // convention.md: Factory Method는 from으로 통일
        return Snapshot.from(version, roadmapId, nodeDtos, edgeDtos, sectionDtos, orphanNodeIds);
    }
}

