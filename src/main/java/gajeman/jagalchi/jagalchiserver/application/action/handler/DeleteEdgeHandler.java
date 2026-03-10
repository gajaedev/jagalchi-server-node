package gajeman.jagalchi.jagalchiserver.application.action.handler;

import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionPayload;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionTarget;
import gajeman.jagalchi.jagalchiserver.domain.event.Event;
import gajeman.jagalchi.jagalchiserver.domain.node.RoadmapEdge;
import gajeman.jagalchi.jagalchiserver.domain.payload.TargetType;
import gajeman.jagalchi.jagalchiserver.global.exception.ActionValidationException;
import gajeman.jagalchi.jagalchiserver.global.exception.EditorException;
import gajeman.jagalchi.jagalchiserver.global.exception.ErrorCode;
import gajeman.jagalchi.jagalchiserver.infrastructure.persistence.RoadmapEdgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 간선(연결선) 삭제 핸들러
 * logic.md: DELETE 액션 (EDGE 대상)
 *
 * 두 노드 간 간선 삭제
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeleteEdgeHandler implements ActionHandler {

    private final RoadmapEdgeRepository roadmapEdgeRepository;

    @Override
    public Event handle(
            Action action
    ) {
        // 1. payload 검증
        validatePayload(action);

        ActionPayload payload = action.getPayload();
        ActionTarget target = payload.getTarget();

        // 2. 간선 ID 추출 및 조회
        String edgeIdStr = target.getObject();
        if (edgeIdStr == null) {
            throw new ActionValidationException(
                    "INVALID_EDGE_ID",
                    "간선 ID가 없습니다"
            );
        }

        Long edgeId = Long.parseLong(edgeIdStr);
        RoadmapEdge edge = roadmapEdgeRepository.findById(edgeId)
                .orElseThrow(() -> new EditorException(ErrorCode.INVALID_INPUT));

        // 3. 간선 정보 저장 (Event에 포함하기 위해)
        Long fromNodeId = edge.getFromNodeId();
        Long toNodeId = edge.getToNodeId();

        // 4. 간선 삭제
        roadmapEdgeRepository.deleteById(edgeId);

        log.info("Edge deleted: edgeId={}, fromNodeId={}, toNodeId={}",
                edgeId, fromNodeId, toNodeId);

        // 5. Event 생성
        return buildEvent(edgeId, fromNodeId, toNodeId);
    }

    /**
     * Payload 검증
     */
    private void validatePayload(
            Action action
    ) {
        ActionPayload payload = action.getPayload();
        if (payload == null) {
            throw new ActionValidationException(
                    "INVALID_PAYLOAD",
                    "payload가 없습니다"
            );
        }

        ActionTarget target = payload.getTarget();
        if (target == null || target.getType() != TargetType.EDGE) {
            throw new ActionValidationException(
                    "INVALID_TARGET",
                    "대상이 간선(EDGE)이어야 합니다"
            );
        }
    }

    /**
     * Event 생성
     */
    private Event buildEvent(
            Long edgeId,
            Long fromNodeId,
            Long toNodeId
    ) {
        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("type", "DELETE");

        // target 정보
        Map<String, Object> target = new HashMap<>();
        target.put("type", "EDGE");
        target.put("object", edgeId.toString());
        eventPayload.put("target", target);

        // 삭제된 간선 정보 (클라이언트가 UI에서 제거할 수 있도록)
        Map<String, Object> state = new HashMap<>();
        state.put("id", edgeId);
        state.put("fromNodeId", fromNodeId);
        state.put("toNodeId", toNodeId);
        eventPayload.put("state", state);

        // Event ID 생성
        String eventId = "evt-" + UUID.randomUUID().toString().substring(0, 8);
        Long sequence = System.currentTimeMillis();

        return Event.from(eventId, sequence, eventPayload);
    }
}

