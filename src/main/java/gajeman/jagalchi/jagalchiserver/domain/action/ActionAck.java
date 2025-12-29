package gajeman.jagalchi.jagalchiserver.domain.action;

import java.time.LocalDateTime;
import java.util.Map;

public record ActionAck(
        String actionId,
        ActionType actionType,
        Map<String, Object> serverState,
        LocalDateTime serverTimestamp
) {
    public static ActionAck from(String actionId, ActionType actionType, Map<String, Object> serverState) {
        return new ActionAck(actionId, actionType, serverState, LocalDateTime.now());
    }
}

