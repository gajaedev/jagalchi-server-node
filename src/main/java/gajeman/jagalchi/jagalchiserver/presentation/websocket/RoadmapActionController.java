package gajeman.jagalchi.jagalchiserver.presentation.websocket;

import gajeman.jagalchi.jagalchiserver.application.action.usecase.ExecuteRoadmapActionUseCase;
import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionAck;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionNack;
import gajeman.jagalchi.jagalchiserver.global.exception.ActionValidationException;
import gajeman.jagalchi.jagalchiserver.global.exception.ActionExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * 로드맵 액션 WebSocket 컨트롤러
 * STOMP 메시지 수신 및 응답 처리
 *
 * logic.md: ACK / NACK / EVENT 처리
 * - ACK: 요청 수락 → /user/queue/ack
 * - NACK: 검증/실행 실패 → /user/queue/nack
 * - EVENT: 상태 변경 → /topic/roadmap/{id}/state
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class RoadmapActionController {

    private final ExecuteRoadmapActionUseCase executeRoadmapActionUseCase;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Action 처리 메서드
     * 클라이언트에서 /app/roadmap/{roadmapId}/action 으로 전송
     *
     * 처리 흐름:
     * 1. 검증 실패 → NACK 반환 (/user/queue/nack)
     * 2. 검증 성공 → UseCase 실행 → ACK 반환 (/user/queue/ack)
     * 3. EVENT 브로드캐스트 (UseCase 내부에서 처리)
     *
     * 헤더:
     * - X-User-ID: 사용자 ID (API Gateway에서 주입)
     * - X-User-Role: 사용자 역할 (ADMIN, USER, GUEST)
     */
    @MessageMapping("/roadmap/{roadmapId}/action")
    public void handleAction(
            @DestinationVariable Long roadmapId,
            @Header(value = "X-User-ID", required = true) String userId,
            @Header(value = "X-User-Role", required = true) String userRole,
            Action action,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        log.info("Received action: actionId={}, roadmapId={}, actionType={}",
                action.getActionId(), roadmapId, action.getAction());

        String sessionId = headerAccessor.getSessionId();
        if (sessionId == null) {
            log.warn("Session ID is null, cannot send response: actionId={}", action.getActionId());
            return;
        }

        try {
            // 1. UseCase 실행 (EVENT 브로드캐스트는 UseCase 내부에서 처리)
            ActionAck ack = executeRoadmapActionUseCase.execute(roadmapId, userId, userRole, action);

            // 2. ACK를 요청자에게만 전송
            messagingTemplate.convertAndSendToUser(
                    sessionId,
                    "/queue/ack",
                    ack
            );

            log.info("ACK sent to user: sessionId={}, actionId={}, status={}",
                    sessionId, action.getActionId(), ack.getStatus());

        } catch (ActionValidationException e) {
            // 검증 실패 → NACK 반환
            ActionNack nack = ActionNack.from(
                    action.getActionId(),
                    action.getAction(),
                    e.getErrorCode(),
                    e.getErrorMessage()
            );

            messagingTemplate.convertAndSendToUser(
                    sessionId,
                    "/queue/nack",
                    nack
            );

            log.warn("NACK sent (validation failed): sessionId={}, actionId={}, errorCode={}",
                    sessionId, action.getActionId(), e.getErrorCode());

        } catch (ActionExecutionException e) {
            // 실행 실패 → NACK 반환
            ActionNack nack = ActionNack.from(
                    action.getActionId(),
                    action.getAction(),
                    e.getErrorCode(),
                    e.getErrorMessage()
            );

            messagingTemplate.convertAndSendToUser(
                    sessionId,
                    "/queue/nack",
                    nack
            );

            log.warn("NACK sent (execution failed): sessionId={}, actionId={}, errorCode={}",
                    sessionId, action.getActionId(), e.getErrorCode());

        } catch (Exception e) {
            // 예상치 못한 에러 → NACK 반환
            ActionNack nack = ActionNack.from(
                    action.getActionId(),
                    action.getAction(),
                    "INTERNAL_ERROR",
                    "An unexpected error occurred: " + e.getMessage()
            );

            messagingTemplate.convertAndSendToUser(
                    sessionId,
                    "/queue/nack",
                    nack
            );

            log.error("NACK sent (unexpected error): sessionId={}, actionId={}",
                    sessionId, action.getActionId(), e);
        }
    }
}


