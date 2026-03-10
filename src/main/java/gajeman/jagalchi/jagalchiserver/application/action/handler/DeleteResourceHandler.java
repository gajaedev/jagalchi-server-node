package gajeman.jagalchi.jagalchiserver.application.action.handler;

import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionPayload;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionTarget;
import gajeman.jagalchi.jagalchiserver.domain.event.Event;
import gajeman.jagalchi.jagalchiserver.domain.node.NodeResource;
import gajeman.jagalchi.jagalchiserver.domain.payload.TargetType;
import gajeman.jagalchi.jagalchiserver.global.exception.ActionValidationException;
import gajeman.jagalchi.jagalchiserver.global.exception.EditorException;
import gajeman.jagalchi.jagalchiserver.global.exception.ErrorCode;
import gajeman.jagalchi.jagalchiserver.infrastructure.persistence.NodeResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 자료(Resource) 삭제 핸들러
 * logic.md: DELETE 액션 (RESOURCE 대상)
 *
 * 노드에서 자료 제거
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeleteResourceHandler implements ActionHandler {

    private final NodeResourceRepository nodeResourceRepository;

    @Override
    public Event handle(
            Action action
    ) {
        // 1. payload 검증
        validatePayload(action);

        ActionPayload payload = action.getPayload();
        ActionTarget target = payload.getTarget();

        // 2. 자료 ID 추출 및 조회
        String resourceIdStr = target.getObject();
        if (resourceIdStr == null) {
            throw new ActionValidationException(
                    "INVALID_RESOURCE_ID",
                    "자료 ID가 없습니다"
            );
        }

        Long resourceId = Long.parseLong(resourceIdStr);
        NodeResource resource = nodeResourceRepository.findById(resourceId)
                .orElseThrow(() -> new EditorException(ErrorCode.INVALID_INPUT));

        // 3. 자료 정보 저장 (Event에 포함하기 위해)
        String title = resource.getTitle();
        Long nodeId = resource.getNodeId();

        // 4. 자료 삭제
        nodeResourceRepository.deleteById(resourceId);

        log.info("Resource deleted: resourceId={}, nodeId={}, title={}",
                resourceId, nodeId, title);

        // 5. Event 생성
        return buildEvent(resourceId, nodeId, title);
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
        if (target == null || target.getType() != TargetType.RESOURCE) {
            throw new ActionValidationException(
                    "INVALID_TARGET",
                    "대상이 자료(RESOURCE)이어야 합니다"
            );
        }
    }

    /**
     * Event 생성
     */
    private Event buildEvent(
            Long resourceId,
            Long nodeId,
            String title
    ) {
        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("type", "DELETE");

        // target 정보
        Map<String, Object> target = new HashMap<>();
        target.put("type", "RESOURCE");
        target.put("object", resourceId.toString());
        eventPayload.put("target", target);

        // 삭제된 자료 정보
        Map<String, Object> state = new HashMap<>();
        state.put("id", resourceId);
        state.put("nodeId", nodeId);
        state.put("title", title);
        eventPayload.put("state", state);

        // Event ID 생성
        String eventId = "evt-" + UUID.randomUUID().toString().substring(0, 8);
        Long sequence = System.currentTimeMillis();

        return Event.from(eventId, sequence, eventPayload);
    }
}

