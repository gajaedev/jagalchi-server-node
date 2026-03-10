package gajeman.jagalchi.jagalchiserver.application.action.handler;

import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionData;
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
 * 자료(Resource) 수정 핸들러
 * logic.md: EDIT 액션 (RESOURCE 대상)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EditResourceHandler implements ActionHandler {

    private final NodeResourceRepository nodeResourceRepository;

    @Override
    public Event handle(Action action) {
        validatePayload(action);

        ActionPayload payload = action.getPayload();
        ActionTarget target = payload.getTarget();
        ActionData data = payload.getData();

        String resourceIdStr = target.getObject();
        if (resourceIdStr == null) {
            throw new ActionValidationException("INVALID_RESOURCE_ID", "자료 ID가 없습니다");
        }

        Long resourceId = Long.parseLong(resourceIdStr);
        NodeResource resource = nodeResourceRepository.findById(resourceId)
                .orElseThrow(() -> new EditorException(ErrorCode.INVALID_INPUT));

        validationCheck(data);
        NodeResource updatedResource = updateResourceProperties(resource, data);
        NodeResource savedResource = nodeResourceRepository.save(updatedResource);

        log.info("Resource updated: resourceId={}, title={}", resourceId, savedResource.getTitle());
        return buildEvent(savedResource);
    }

    private NodeResource updateResourceProperties(NodeResource resource, ActionData data) {
        if (data == null) {
            return resource;
        }

        return NodeResource.builder()
                .id(resource.getId())
                .unitId(resource.getUnitId())
                .nodeId(resource.getNodeId())
                .resourceType(resource.getResourceType())
                .title(data.getResourceTitle() != null && !data.getResourceTitle().trim().isEmpty()
                    ? data.getResourceTitle() : resource.getTitle())
                .url(data.getResourceUrl() != null && !data.getResourceUrl().trim().isEmpty()
                    ? data.getResourceUrl() : resource.getUrl())
                .description(data.getResourceDescription() != null
                    ? data.getResourceDescription() : resource.getDescription())
                .metadata(data.getResourceMetadata() != null
                    ? data.getResourceMetadata() : resource.getMetadata())
                .displayOrder(data.getDisplayOrder() != null
                    ? data.getDisplayOrder() : resource.getDisplayOrder())
                .build();
    }

    private void validationCheck(ActionData data) {
        if (data == null) {
            return;
        }

        if (data.getResourceTitle() != null && data.getResourceTitle().length() > 255) {
            throw new ActionValidationException(
                    "RESOURCE_TITLE_TOO_LONG",
                    "자료 제목은 255자 이하여야 합니다"
            );
        }

        if (data.getDisplayOrder() != null && data.getDisplayOrder() < 0) {
            throw new ActionValidationException(
                    "INVALID_DISPLAY_ORDER",
                    "표시 순서는 0 이상이어야 합니다"
            );
        }
    }

    private void validatePayload(Action action) {
        ActionPayload payload = action.getPayload();
        if (payload == null) {
            throw new ActionValidationException("INVALID_PAYLOAD", "payload가 없습니다");
        }

        ActionTarget target = payload.getTarget();
        if (target == null || target.getType() != TargetType.RESOURCE) {
            throw new ActionValidationException("INVALID_TARGET", "대상이 자료(RESOURCE)이어야 합니다");
        }
    }

    private Event buildEvent(NodeResource updatedResource) {
        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("type", "EDIT");

        Map<String, Object> target = new HashMap<>();
        target.put("type", "RESOURCE");
        target.put("object", updatedResource.getId().toString());
        eventPayload.put("target", target);

        eventPayload.put("state", updatedResource.toDto());

        String eventId = "evt-" + UUID.randomUUID().toString().substring(0, 8);
        Long sequence = System.currentTimeMillis();

        return Event.from(eventId, sequence, eventPayload);
    }
}

