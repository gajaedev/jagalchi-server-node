package gajeman.jagalchi.jagalchiserver.presentation.websocket;

import gajeman.jagalchi.jagalchiserver.application.snapshot.usecase.GetSnapshotUseCase;
import gajeman.jagalchi.jagalchiserver.domain.event.Snapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

/**
 * WebSocket 구독 이벤트 핸들러
 * logic.md: 초기 연결 시 SNAPSHOT 전송
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SnapshotEventHandler {

    private final GetSnapshotUseCase getSnapshotUseCase;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 클라이언트가 /topic/roadmap/{roadmapId}/state를 구독할 때 SNAPSHOT 전송
     *
     * @param event 구독 이벤트
     */
    @EventListener
    public void handleSubscribeEvent(
            SessionSubscribeEvent event
    ) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headers.getDestination();

        // /topic/roadmap/{roadmapId}/state 구독 확인
        if (destination != null && destination.startsWith("/topic/roadmap/") && destination.endsWith("/state")) {
            // roadmapId 추출
            String roadmapId = extractRoadmapId(destination);
            String sessionId = headers.getSessionId();

            log.info("Client subscribed to state topic: sessionId={}, roadmapId={}", sessionId, roadmapId);

            // SNAPSHOT 생성
            Snapshot snapshot = getSnapshotUseCase.getSnapshot(roadmapId);

            // 구독한 클라이언트에게만 SNAPSHOT 전송
            messagingTemplate.convertAndSendToUser(
                    sessionId,
                    "/queue/snapshot",
                    snapshot
            );

            log.info("SNAPSHOT sent to user: sessionId={}, roadmapId={}, version={}",
                    sessionId, roadmapId, snapshot.getVersion());
        }
    }

    /**
     * destination에서 roadmapId 추출
     *
     * @param destination /topic/roadmap/{roadmapId}/state
     * @return roadmapId
     */
    private String extractRoadmapId(
            String destination
    ) {
        // /topic/roadmap/{roadmapId}/state → roadmapId
        String[] parts = destination.split("/");
        if (parts.length >= 4) {
            return parts[3];
        }
        return null;
    }
}

