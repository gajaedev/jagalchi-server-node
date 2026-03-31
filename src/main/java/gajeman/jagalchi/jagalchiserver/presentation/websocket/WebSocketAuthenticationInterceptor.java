package gajeman.jagalchi.jagalchiserver.presentation.websocket;

import gajeman.jagalchi.jagalchiserver.global.exception.EditorException;
import gajeman.jagalchi.jagalchiserver.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

/**
 * WebSocket 인증 인터셉터
 * 
 * CONNECT 시 HTTP 업그레이드 요청의 헤더를 세션 속성으로 저장
 * SEND 시 세션 속성에서 userId와 userRole을 가져와 사용
 */
@Component
@Slf4j
public class WebSocketAuthenticationInterceptor implements ChannelInterceptor {

    private static final String HEADER_USER_ID = "X-User-ID";
    private static final String HEADER_USER_ROLE = "X-User-Role";
    private static final String HEADER_ROADMAP_ID = "X-Roadmap-ID";
    private static final String HEADER_PERMISSIONS = "X-Permissions";
    
    // 세션 속성 키
    private static final String SESSION_USER_ID = "userId";
    private static final String SESSION_USER_ROLE = "userRole";
    private static final String SESSION_ROADMAP_ID = "roadmapId";
    private static final String SESSION_PERMISSIONS = "permissions";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        if (command == null) {
            return message;
        }

        // CONNECT 시 헤더에서 userId, userRole, permissions, roadmapId 추출하여 세션에 저장
        if (StompCommand.CONNECT.equals(command)) {
            String userId = accessor.getFirstNativeHeader(HEADER_USER_ID);
            String userRole = accessor.getFirstNativeHeader(HEADER_USER_ROLE);
            String roadmapId = accessor.getFirstNativeHeader(HEADER_ROADMAP_ID);
            String permissions = accessor.getFirstNativeHeader(HEADER_PERMISSIONS);
            
            if (userId != null && userRole != null) {
                if (accessor.getSessionAttributes() != null) {
                    accessor.getSessionAttributes().put(SESSION_USER_ID, userId);
                    accessor.getSessionAttributes().put(SESSION_USER_ROLE, userRole);
                    if (roadmapId != null) {
                        accessor.getSessionAttributes().put(SESSION_ROADMAP_ID, roadmapId);
                    }
                    if (permissions != null) {
                        accessor.getSessionAttributes().put(SESSION_PERMISSIONS, permissions);
                    }
                }
                log.info("WebSocket CONNECT: userId={}, userRole={}, roadmapId={}, permissions={}", userId, userRole, roadmapId, permissions);
            } else {
                log.warn("WebSocket CONNECT without auth headers, using guest mode");
            }
        }
        
        // SUBSCRIBE와 SEND 시 세션에서 userId 확인 (선택적)
        else if (StompCommand.SUBSCRIBE.equals(command) || StompCommand.SEND.equals(command)) {
            String destination = accessor.getDestination();
            Object userId = accessor.getSessionAttributes().get(SESSION_USER_ID);
            Object userRole = accessor.getSessionAttributes().get(SESSION_USER_ROLE);
            
            log.debug("WebSocket {}: destination={}, userId={}, userRole={}", 
                    command, destination, userId, userRole);
        }

        return message;
    }
}




