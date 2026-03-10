package gajeman.jagalchi.jagalchiserver.application.action.pipeline;

import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.auth.ActionContext;
import org.springframework.stereotype.Component;

@Component
public class ActionPipelineValidator {
    public void validate(Action action, ActionContext context) {
        // ...기존 검증 로직...
    }
    public void markAsProcessed(String actionId) {
        // ...기존 마킹 로직 (actionId is String in domain.Action)
    }
}
