package gajeman.jagalchi.jagalchiserver.application.action;

import gajeman.jagalchi.jagalchiserver.application.action.handler.ActionHandler;
import gajeman.jagalchi.jagalchiserver.application.action.handler.NodeCreateHandler;
import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionAck;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionType;
import gajeman.jagalchi.jagalchiserver.domain.auth.ActionContext;
import gajeman.jagalchi.jagalchiserver.global.exception.CustomException;
import gajeman.jagalchi.jagalchiserver.global.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ActionDispatcher {

    private final NodeCreateHandler nodeCreateHandler;
    private final ActionPipelineValidator pipelineValidator;
    // TODO: 새로운 핸들러 추가 시 여기에 주입
    // private final NodeMoveHandler nodeMoveHandler;

    private Map<ActionType, ActionHandler> handlerMap;

    @PostConstruct
    public void init() {
        handlerMap = new EnumMap<>(ActionType.class);
        handlerMap.put(ActionType.NODE_CREATE, nodeCreateHandler);
        // TODO: 새로운 핸들러 추가 시 여기에 등록
        // handlerMap.put(ActionType.NODE_MOVE, nodeMoveHandler);
    }

    /**
     * Action 처리 파이프라인 (logic.md 기준)
     * 1. actionId 중복 여부 확인
     * 2. 로드맵 존재 확인 (TODO)
     * 3. 사용자 편집 권한 확인
     * 4. 로드맵 편집 가능 상태 확인 (TODO)
     * 5. actionType에 맞는 Validator 실행
     * 6. actionType에 맞는 Executor 실행
     * 7. 변경 사항 DB 저장
     * 8. 로드맵 캐시 무효화 (TODO)
     * 9. ACK 반환
     */
    @Transactional
    public ActionAck dispatch(Action action, ActionContext context) {
        // 1~4. 공통 파이프라인 검증
        pipelineValidator.validate(action, context);

        // 5~7. 핸들러별 처리 (Validator + Executor + 저장)
        ActionHandler handler = handlerMap.get(action.getActionType());
        if (handler == null) {
            log.warn("Unsupported actionType={}, actionId={}", action.getActionType(), action.getActionId());
            throw new CustomException(ErrorCode.INVALID_ACTION_TYPE);
        }

        ActionAck ack = handler.handle(action);
        if (ack == null) {
            log.error("Handler returned null ActionAck for actionId={} actionType={}", action.getActionId(), action.getActionType());
            // 처리 실패로 간주
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 8. 처리된 actionId 마킹 (중복 처리 방지)
        pipelineValidator.markAsProcessed(action.getActionId());

        // 9. TODO: 로드맵 캐시 무효화
        // cacheManager.evict("roadmap:" + action.getRoadmapId());

        // 10. ACK 반환
        log.debug("Action dispatched successfully actionId={} actionType={}", action.getActionId(), action.getActionType());
        return ack;
    }

    /**
     * @deprecated ActionContext 없이 호출하는 것은 권장하지 않음
     * 기존 호환성을 위해 유지, 향후 제거 예정
     */
    @Deprecated
    public ActionAck dispatch(Action action) {
        // actorId가 있으면 ActionContext 생성
        if (action.getActorId() != null) {
            ActionContext context = ActionContext.of(action.getActorId(),
                    gajeman.jagalchi.jagalchiserver.domain.auth.UserRole.USER);
            return dispatch(action, context);
        }
        throw new CustomException(ErrorCode.UNAUTHORIZED);
    }
}
