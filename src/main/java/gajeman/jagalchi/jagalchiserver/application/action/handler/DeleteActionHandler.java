package gajeman.jagalchi.jagalchiserver.application.action.handler;

import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionPayload;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionTarget;
import gajeman.jagalchi.jagalchiserver.domain.event.Event;
import gajeman.jagalchi.jagalchiserver.domain.node.RoadmapEdge;
import gajeman.jagalchi.jagalchiserver.domain.node.RoadmapNode;
import gajeman.jagalchi.jagalchiserver.domain.payload.TargetType;
import gajeman.jagalchi.jagalchiserver.global.exception.EditorException;
import gajeman.jagalchi.jagalchiserver.global.exception.ErrorCode;
import gajeman.jagalchi.jagalchiserver.infrastructure.persistence.RoadmapEdgeRepository;
import gajeman.jagalchi.jagalchiserver.infrastructure.persistence.RoadmapNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DELETE 액션 핸들러
 * logic.md: 노드 삭제 및 연결된 엣지 제거
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeleteActionHandler implements ActionHandler {

    private final RoadmapNodeRepository roadmapNodeRepository;
    private final RoadmapEdgeRepository roadmapEdgeRepository;

    @Override
    public Event handle(
            Action action
    ) {
        // 1. payload 검증
        validatePayload(action);

        ActionPayload payload = action.getPayload();
        ActionTarget target = payload.getTarget();
        String nodeId = target.getObject();

        // 2. 노드 존재 확인
        RoadmapNode node = roadmapNodeRepository.findById(Long.parseLong(nodeId))
                .orElseThrow(() -> new EditorException(ErrorCode.NODE_NOT_FOUND));

        // 3. 연결된 엣지 조회 및 삭제
        List<RoadmapEdge> connectedEdges = roadmapEdgeRepository
                .findByFromNodeIdOrToNodeId(Long.parseLong(nodeId), Long.parseLong(nodeId));

        if (!connectedEdges.isEmpty()) {
            roadmapEdgeRepository.deleteAll(connectedEdges);
            log.info("Deleted {} connected edges for node: nodeId={}",
                    connectedEdges.size(), nodeId);
        }

        // 4. 노드 삭제 전 정보 저장 (EVENT용)
        Map<String, Object> deletedNodeInfo = node.toDto();

        // 5. 노드 삭제
        roadmapNodeRepository.delete(node);

        log.info("Node deleted: nodeId={}, connectedEdges={}", nodeId, connectedEdges.size());

        // 6. Event 생성
        return buildEvent(nodeId, deletedNodeInfo);
    }

    /**
     * payload 검증
     */
    private void validatePayload(
            Action action
    ) {
        ActionPayload payload = action.getPayload();
        if (payload == null) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }

        // target 검증
        ActionTarget target = payload.getTarget();
        if (target == null || target.getType() != TargetType.NODE) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }

        // object (nodeId) 검증
        if (target.getObject() == null) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * Event 객체 생성
     */
    private Event buildEvent(
            String nodeId,
            Map<String, Object> deletedNodeInfo
    ) {
        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("type", "DELETE");

        // target 정보
        Map<String, Object> target = new HashMap<>();
        target.put("type", "NODE");
        target.put("object", nodeId);
        eventPayload.put("target", target);

        // 삭제된 노드 정보 (클라이언트가 UI에서 제거하기 위해)
        eventPayload.put("deletedNode", deletedNodeInfo);

        // Event ID 생성 (UUID 사용)
        String eventId = "evt-" + UUID.randomUUID().toString().substring(0, 8);

        // Sequence는 시스템 시간 사용
        Long sequence = System.currentTimeMillis();

        return Event.from(eventId, sequence, eventPayload);
    }
}

