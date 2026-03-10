package gajeman.jagalchi.jagalchiserver.application.action.service;

import gajeman.jagalchi.jagalchiserver.application.action.dispatcher.ActionDispatcher;
import gajeman.jagalchi.jagalchiserver.application.action.usecase.ExecuteRoadmapActionUseCase;
import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionAck;
import gajeman.jagalchi.jagalchiserver.domain.auth.ActionContext;
import gajeman.jagalchi.jagalchiserver.domain.auth.UserRole;
import gajeman.jagalchi.jagalchiserver.domain.event.Event;
import gajeman.jagalchi.jagalchiserver.domain.history.ActionHistory;
import gajeman.jagalchi.jagalchiserver.infrastructure.persistence.ActionHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 로드맵 액션 실행 유즈케이스 구현체
 * Action 처리 및 EVENT 브로드캐스트 담당
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExecuteRoadmapActionUseCaseImpl implements ExecuteRoadmapActionUseCase {

    private final ActionDispatcher actionDispatcher;
    private final SimpMessagingTemplate messagingTemplate;
    private final ActionHistoryRepository actionHistoryRepository;

    /**
     * 액션 실행
     *
     * @param roadmapId 로드맵 ID
     * @param userId    사용자 ID
     * @param userRole  사용자 역할
     * @param action    액션 객체
     * @return 처리 결과 Ack
     */
    @Override
    public ActionAck execute(
            Long roadmapId,
            String userId,
            String userRole,
            Action action
    ) {
        // 1. ActionContext 생성
        ActionContext context = ActionContext.builder()
                .userId(userId != null ? Long.parseLong(userId) : null)
                .userRole(parseUserRole(userRole))
                .build();

        log.info("Executing action: actionId={}, roadmapId={}, userId={}, actionType={}",
                action.getActionId(), roadmapId, userId, action.getAction());

        // 2. ActionDispatcher를 통해 처리
        Event event = actionDispatcher.dispatch(action, context);

        // 3. ActionHistory에 저장 (EVENT 조회 및 UNDO/REDO 기능용)
        saveActionHistory(action, event, userId);

        // 4. EVENT를 /topic/roadmap/{roadmapId}/state 로 브로드캐스트
        String stateTopic = "/topic/roadmap/" + action.getRoadmap() + "/state";
        messagingTemplate.convertAndSend(stateTopic, event);

        log.info("Event broadcasted: eventId={}, topic={}", event.getEventId(), stateTopic);

        // 5. ACK 생성 및 반환 (Controller에서 요청자에게 전송)
        return ActionAck.from(action.getActionId(), "ACCEPTED");
    }

    /**
     * ActionHistory 저장
     * EVENT 조회 및 UNDO/REDO 기능에 사용
     */
    private void saveActionHistory(
            Action action,
            Event event,
            String userId
    ) {
        // prevState 추출 (이전 상태 저장)
        // logic.md: UNDO 시 복원할 이전 상태를 저장
        Map<String, Object> prevState = extractPrevState(event);

        ActionHistory history = ActionHistory.builder()
                .roadmapId(action.getRoadmap())
                .userId(userId != null ? Long.parseLong(userId) : null)
                .actionType(action.getAction())
                .eventId(event.getEventId())
                .sequence(event.getSequence())
                .eventPayload(event.getPayload())
                .prevState(prevState)  // UNDO용 이전 상태
                .build();

        actionHistoryRepository.save(history);

        log.debug("ActionHistory saved: eventId={}, sequence={}, hasPrevState={}",
                event.getEventId(), event.getSequence(), prevState != null);
    }

    /**
     * EVENT payload에서 이전 상태 추출
     * ACTION 타입에 따라 이전 상태를 결정
     */
    private Map<String, Object> extractPrevState(
            Event event
    ) {
        // TODO: 현재 상태를 조회하여 prevState 설정
        // 예: 노드의 현재 상태를 DB에서 조회 후 저장
        // 임시로 null 반환
        return null;
    }

    /**
     * UserRole 문자열을 Enum으로 변환
     */
    private UserRole parseUserRole(
            String userRole
    ) {
        if (userRole == null) {
            return UserRole.GUEST;
        }
        try {
            return UserRole.valueOf(userRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid userRole: {}, defaulting to GUEST", userRole);
            return UserRole.GUEST;
        }
    }
}

