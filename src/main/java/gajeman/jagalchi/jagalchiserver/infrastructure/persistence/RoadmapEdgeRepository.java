package gajeman.jagalchi.jagalchiserver.infrastructure.persistence;

import gajeman.jagalchi.jagalchiserver.domain.node.RoadmapEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 로드맵 간선(Edge) 저장소
 */
@Repository
public interface RoadmapEdgeRepository extends JpaRepository<RoadmapEdge, Long> {

    /**
     * 특정 노드와 연결된 모든 엣지 조회
     * 노드 삭제 시 연결된 엣지도 함께 삭제하기 위해 사용
     *
     * @param fromNodeId 시작 노드 ID
     * @param toNodeId 도착 노드 ID
     * @return 해당 노드와 연결된 엣지 목록
     */
    List<RoadmapEdge> findByFromNodeIdOrToNodeId(
            Long fromNodeId,
            Long toNodeId
    );

    /**
     * 특정 로드맵의 모든 간선 조회
     */
    List<RoadmapEdge> findByUnitId(
            Long unitId
    );

    /**
     * 특정 노드에서 출발하는 모든 간선 조회
     */
    List<RoadmapEdge> findOutgoingEdges(
            Long unitId,
            Long nodeId
    );

    /**
     * 특정 노드로 들어오는 모든 간선 조회
     */
    List<RoadmapEdge> findIncomingEdges(
            Long unitId,
            Long nodeId
    );

    /**
     * 두 노드 사이의 간선 존재 여부 확인 (중복 검사용)
     */
    Optional<RoadmapEdge> findExistingEdge(
            Long unitId,
            Long fromNodeId,
            Long toNodeId
    );

    /**
     * 특정 로드맵에서 연결된 모든 노드 ID 조회
     * 고아 노드 검출용: 간선이 있는 모든 노드 ID를 반환
     * QueryDSL로 구현 필요
     */
    List<Long> findAllConnectedNodeIds(
            Long unitId
    );
}
