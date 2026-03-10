package gajeman.jagalchi.jagalchiserver.application.action.dispatcher;

import gajeman.jagalchi.jagalchiserver.application.action.handler.ActionHandler;
import gajeman.jagalchi.jagalchiserver.application.action.handler.CreateActionHandler;
import gajeman.jagalchi.jagalchiserver.application.action.handler.EditActionHandler;
import gajeman.jagalchi.jagalchiserver.application.action.handler.DeleteActionHandler;
import gajeman.jagalchi.jagalchiserver.application.action.handler.UndoActionHandler;
import gajeman.jagalchi.jagalchiserver.application.action.handler.RedoActionHandler;
import gajeman.jagalchi.jagalchiserver.application.action.handler.EditStateChangeHandler;
import gajeman.jagalchi.jagalchiserver.application.action.pipeline.ActionPipelineValidator;
import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionType;
import gajeman.jagalchi.jagalchiserver.domain.auth.ActionContext;
import gajeman.jagalchi.jagalchiserver.domain.event.Event;
import gajeman.jagalchi.jagalchiserver.global.exception.EditorException;
import gajeman.jagalchi.jagalchiserver.global.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Action 디스패처
 * ActionType에 따라 적절한 핸들러로 라우팅
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ActionDispatcher {

    private final CreateActionHandler createActionHandler;
    private final EditActionHandler editActionHandler;
    private final DeleteActionHandler deleteActionHandler;
    private final UndoActionHandler undoActionHandler;
    private final RedoActionHandler redoActionHandler;
    private final EditStateChangeHandler editStateChangeHandler;
    private final ActionPipelineValidator pipelineValidator;

    private Map<ActionType, ActionHandler> handlerMap = Collections.emptyMap();

    @PostConstruct
    private void init() {
        EnumMap<ActionType, ActionHandler> map = new EnumMap<>(ActionType.class);

        // CREATE 핸들러 등록
        if (createActionHandler != null) {
            map.put(ActionType.CREATE, createActionHandler);
        }

        // EDIT 핸들러 등록
        if (editActionHandler != null) {
            map.put(ActionType.EDIT, editActionHandler);
        }

        // DELETE 핸들러 등록
        if (deleteActionHandler != null) {
            map.put(ActionType.DELETE, deleteActionHandler);
        }

        // UNDO 핸들러 등록
        if (undoActionHandler != null) {
            map.put(ActionType.UNDO, undoActionHandler);
        }

        // REDO 핸들러 등록
        if (redoActionHandler != null) {
            map.put(ActionType.REDO, redoActionHandler);
        }

        handlerMap = Collections.unmodifiableMap(map);
        log.info("ActionDispatcher initialized with handlers={}", handlerMap.keySet());
    }

    /**
     * Action 처리 파이프라인 실행
     *
     * @param action  처리할 액션 (not null)
     * @param context 인증/인가 컨텍스트 (not null)
     * @return Event 처리 결과 이벤트
     * @throws EditorException 처리 실패 시
     */
    @Transactional
    public Event dispatch(
            Action action,
            ActionContext context
    ) {
        Objects.requireNonNull(action, "action must not be null");
        Objects.requireNonNull(context, "action context must not be null");

        // 1. 공통 파이프라인 검증
        pipelineValidator.validate(action, context);

        // 2. 핸들러 조회 및 실행
        ActionType type = action.getAction();
        if (type == null) {
            log.warn("Action has no actionType, actionId={}", action.getActionId());
            throw new EditorException(ErrorCode.INVALID_ACTION_TYPE);
        }

        ActionHandler handler = handlerMap.get(type);
        if (handler == null) {
            log.warn("No handler registered for actionType={}, actionId={}", type, action.getActionId());
            throw new EditorException(ErrorCode.INVALID_ACTION_TYPE);
        }

        Event event = handler.handle(action);
        if (event == null) {
            log.error("Handler returned null EVENT for actionId={} actionType={}",
                    action.getActionId(), type);
            throw new EditorException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 3. 처리 완료 마킹
        pipelineValidator.markAsProcessed(action.getActionId());

        log.debug("Action dispatched successfully: actionId={} actionType={} eventId={}",
                action.getActionId(), type, event.getEventId());

        return event;
    }
}

