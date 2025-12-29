package gajeman.jagalchi.jagalchiserver.presentation.websocket;

import gajeman.jagalchi.jagalchiserver.application.action.ActionDispatcher;
import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionAck;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionNack;
import gajeman.jagalchi.jagalchiserver.domain.auth.ActionContext;
import gajeman.jagalchi.jagalchiserver.global.exception.CustomException;
import gajeman.jagalchi.jagalchiserver.global.exception.ErrorCode;
import gajeman.jagalchi.jagalchiserver.infrastructure.auth.ActionContextExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class RoadmapActionController {

    private final ActionDispatcher actionDispatcher;
    private final SimpMessagingTemplate messagingTemplate;
    private final ActionContextExtractor actionContextExtractor;

    /**
     * Action 처리 메서드
     * 클라이언트에서 /app/roadmap/{roadmapId}/action 으로 전송
     * 성공 시 /topic/roadmap/{roadmapId} 로 브로드캐스트
     * 실패 시 요청자에게만 NACK 전송
     *
     * 헤더:
     * - X-User-Id: 사용자 ID
     * - X-User-Role: 사용자 역할 (ADMIN, USER, GUEST)
     */
    @MessageMapping("/roadmap/{roadmapId}/action")
    @SendTo("/topic/roadmap/{roadmapId}")
    public ActionAck handleAction(
            @DestinationVariable Long roadmapId,
            @Header(value = "X-User-Id", required = false) String userId,
            @Header(value = "X-User-Role", required = false) String userRole,
            Action action
    ) {
        // 기본 입력 검증
        if (action == null) {
            log.warn("Received null action for roadmapId={}", roadmapId);
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        if (action.getRoadmapId() != null && !action.getRoadmapId().equals(roadmapId)) {
            log.warn("RoadmapId mismatch: pathRoadmapId={} actionRoadmapId={}", roadmapId, action.getRoadmapId());
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        try {
            ActionContext context = actionContextExtractor.extract(userId, userRole);
            ActionAck ack = actionDispatcher.dispatch(action, context);
            return ack;
        } catch (CustomException exception) {
            ActionNack nack = ActionNack.from(
                    action.getActionId(),
                    action.getActionType(),
                    exception.getErrorCode().getCode(),
                    exception.getErrorCode().getMessage()
            );
            // NACK 전송은 이후 커밋에서 더 안전하게 처리
            String targetUser = action.getActorId() != null
                    ? action.getActorId().toString()
                    : userId;
            if (targetUser != null) {
                messagingTemplate.convertAndSendToUser(
                        targetUser,
                        "/queue/errors",
                        nack
                );
            }
            return null;
        }
    }
}
