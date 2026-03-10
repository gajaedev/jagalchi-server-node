package gajeman.jagalchi.jagalchiserver.application.action.handler;

import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionData;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionPayload;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionTarget;
import gajeman.jagalchi.jagalchiserver.domain.event.Event;
import gajeman.jagalchi.jagalchiserver.domain.node.NodeResource;
import gajeman.jagalchi.jagalchiserver.domain.payload.TargetType;
import gajeman.jagalchi.jagalchiserver.global.exception.ActionValidationException;
import gajeman.jagalchi.jagalchiserver.infrastructure.persistence.NodeResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 자료(Resource) 생성 핸들러
 * logic.md: CREATE 액션 (RESOURCE 대상)
 *
 * 노드에 학습 자료 추가
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CreateResourceHandler implements ActionHandler {

    private final NodeResourceRepository nodeResourceRepository;

    @Override
    public Event handle(
            Action action
    ) {
        // 1. payload 검증
        validatePayload(action);

        ActionPayload payload = action.getPayload();
        ActionTarget target = payload.getTarget();
        ActionData data = payload.getData();

        // 2. 필수 데이터 추출
        Long nodeId = target.getNodeId();
        String title = data.getResourceTitle();
        String resourceType = data.getResourceType();
        String url = data.getResourceUrl();

        if (nodeId == null) {
            throw new ActionValidationException(
                    "INVALID_NODE_ID",
                    "노드 ID가 필요합니다"
            );
        }

        if (title == null || title.trim().isEmpty()) {
            throw new ActionValidationException(
                    "INVALID_RESOURCE_TITLE",
                    "자료 제목이 필요합니다"
            );
        }

        if (resourceType == null || resourceType.trim().isEmpty()) {
            throw new ActionValidationException(
                    "INVALID_RESOURCE_TYPE",
                    "자료 타입이 필요합니다"
            );
        }

        if (url == null || url.trim().isEmpty()) {
            throw new ActionValidationException(
                    "INVALID_RESOURCE_URL",
                    "자료 URL이 필요합니다"
            );
        }

        // 3. unitId 추출
        Long unitId = Long.parseLong(action.getRoadmap());

        // 4. 자료 생성
        NodeResource resource = NodeResource.builder()
                .unitId(unitId)
                .nodeId(nodeId)
                .title(title)
                .resourceType(resourceType)
                .url(url)
                .description(data.getResourceDescription())
                .metadata(data.getResourceMetadata())
                .displayOrder(data.getDisplayOrder() != null ? data.getDisplayOrder() : 0)
                .build();

        NodeResource createdResource = nodeResourceRepository.save(resource);

        log.info("Resource created: id={}, nodeId={}, title={}, type={}",
                createdResource.getId(), nodeId, title, resourceType);

        // 5. Event 생성
        return buildEvent(createdResource);
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
            NodeResource createdResource
    ) {
        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("type", "CREATE");

        // target 정보
        Map<String, Object> target = new HashMap<>();
        target.put("type", "RESOURCE");
        target.put("object", createdResource.getId().toString());
        eventPayload.put("target", target);

        // 생성된 자료 상태
        eventPayload.put("state", createdResource.toDto());

        // Event ID 생성
        String eventId = "evt-" + UUID.randomUUID().toString().substring(0, 8);
        Long sequence = System.currentTimeMillis();

        return Event.from(eventId, sequence, eventPayload);
    }
}

