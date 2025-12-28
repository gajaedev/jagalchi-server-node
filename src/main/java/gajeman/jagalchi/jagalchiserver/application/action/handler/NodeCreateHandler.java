package gajeman.jagalchi.jagalchiserver.application.action.handler;

import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionAck;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionType;
import gajeman.jagalchi.jagalchiserver.domain.node.RoadmapNode;
import gajeman.jagalchi.jagalchiserver.global.exception.CustomException;
import gajeman.jagalchi.jagalchiserver.global.exception.ErrorCode;
import gajeman.jagalchi.jagalchiserver.infrastructure.persistence.RoadmapNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NodeCreateHandler implements ActionHandler {

    private final RoadmapNodeRepository roadmapNodeRepository;

    @Override
    public ActionAck handle(Action action) {
        validatePayload(action);

        Map<String, Object> payload = action.getPayload();
        String tempId = (String) payload.get("tempId");

        RoadmapNode roadmapNode = RoadmapNode.builder()
                .unitId(action.getRoadmapId())
                .label((String) payload.get("label"))
                .x(((Number) payload.get("x")).floatValue())
                .y(((Number) payload.get("y")).floatValue())
                .data((Map<String, Object>) payload.get("data"))
                .build();

        RoadmapNode savedNode = roadmapNodeRepository.save(roadmapNode);

        Map<String, Object> serverState = new HashMap<>();
        serverState.put("tempId", tempId);
        serverState.put("nodeId", savedNode.getId());
        serverState.put("label", savedNode.getLabel());
        serverState.put("x", savedNode.getX());
        serverState.put("y", savedNode.getY());
        serverState.put("data", savedNode.getData());

        return ActionAck.from(action.getActionId(), ActionType.NODE_CREATE, serverState);
    }

    private void validatePayload(Action action) {
        Map<String, Object> payload = action.getPayload();
        if (payload == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        if (payload.get("x") == null || payload.get("y") == null) {
            throw new CustomException(ErrorCode.NODE_POSITION_REQUIRED);
        }
    }
}

