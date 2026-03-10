package gajeman.jagalchi.jagalchiserver.domain.action;

public record ActionNack(
        String actionId,
        ActionType actionType,
        String errorCode,
        String errorMessage
) {
    public static ActionNack from(String actionId, ActionType actionType, String errorCode, String errorMessage) {
        return new ActionNack(actionId, actionType, errorCode, errorMessage);
    }
}

