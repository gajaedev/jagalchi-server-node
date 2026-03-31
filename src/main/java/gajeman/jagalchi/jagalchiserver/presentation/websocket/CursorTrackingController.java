package gajeman.jagalchi.jagalchiserver.presentation.websocket;

import gajeman.jagalchi.jagalchiserver.presentation.websocket.dto.CursorPosition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * 커서 위치 추적 WebSocket 컨트롤러
 * 실시간 협업을 위한 사용자 커서 위치 브로드캐스트
 * 
 * 메시지 흐름:
 * 1. 클라이언트 → /app/roadmap/{roadmapId}/cursor (커서 위치 전송)
 * 2. 서버 → /topic/roadmap/{roadmapId}/cursors (다른 사용자들에게 브로드캐스트)
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class CursorTrackingController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 커서 위치 업데이트
     * 
     * @param roadmapId 로드맵 ID
     * @param userId 사용자 ID (API Gateway에서 주입)
     * @param cursorPosition 커서 위치 정보
     */
    @MessageMapping("/roadmap/{roadmapId}/cursor")
    public void updateCursorPosition(
            @DestinationVariable Long roadmapId,
            @Header(value = "X-User-ID", required = true) String userId,
            CursorPosition cursorPosition
    ) {
        try {
            // userId 설정 (클라이언트가 보내지 않을 경우 헤더 값 사용)
            if (cursorPosition.getUserId() == null) {
                cursorPosition = CursorPosition.builder()
                        .userId(Long.parseLong(userId))
                        .userName(cursorPosition.getUserName())
                        .x(cursorPosition.getX())
                        .y(cursorPosition.getY())
                        .timestamp(System.currentTimeMillis())
                        .state(cursorPosition.getState())
                        .targetId(cursorPosition.getTargetId())
                        .build();
            }

            // 같은 로드맵의 다른 사용자들에게 커서 위치 브로드캐스트
            messagingTemplate.convertAndSend(
                    "/topic/roadmap/" + roadmapId + "/cursors",
                    cursorPosition
            );

            log.debug("Cursor position broadcasted: roadmapId={}, userId={}, x={}, y={}, state={}",
                    roadmapId, userId, cursorPosition.getX(), cursorPosition.getY(), cursorPosition.getState());

        } catch (Exception e) {
            log.error("Failed to broadcast cursor position: roadmapId={}, userId={}, error={}",
                    roadmapId, userId, e.getMessage());
        }
    }

    /**
     * 커서 숨김 (사용자가 로드맵을 떠날 때)
     * 
     * @param roadmapId 로드맵 ID
     * @param userId 사용자 ID
     */
    @MessageMapping("/roadmap/{roadmapId}/cursor/hide")
    public void hideCursor(
            @DestinationVariable Long roadmapId,
            @Header(value = "X-User-ID", required = true) String userId
    ) {
        try {
            CursorPosition hiddenCursor = CursorPosition.builder()
                    .userId(Long.parseLong(userId))
                    .timestamp(System.currentTimeMillis())
                    .build();

            // 커서 제거 이벤트 브로드캐스트
            messagingTemplate.convertAndSend(
                    "/topic/roadmap/" + roadmapId + "/cursors/hide",
                    hiddenCursor
            );

            log.debug("Cursor hidden: roadmapId={}, userId={}", roadmapId, userId);

        } catch (Exception e) {
            log.error("Failed to hide cursor: roadmapId={}, userId={}, error={}",
                    roadmapId, userId, e.getMessage());
        }
    }
}
