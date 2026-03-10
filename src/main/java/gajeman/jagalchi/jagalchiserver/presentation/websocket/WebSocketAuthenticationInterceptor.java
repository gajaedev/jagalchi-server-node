package gajeman.jagalchi.jagalchiserver.presentation.websocket;

import gajeman.jagalchi.jagalchiserver.global.exception.EditorException;
import gajeman.jagalchi.jagalchiserver.global.exception.ErrorCode;
import gajeman.jagalchi.jagalchiserver.presentation.util.PermissionValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

/**
 * WebSocket 권한 검증 인터셉터
 * STOMP 메시지 기반 권한 검증
 */
@Component
@Slf4j
public class WebSocketAuthenticationInterceptor implements ChannelInterceptor {

    private static final String HEADER_USER_ID = "X-User-ID";
    private static final String HEADER_ROADMAP_ID = "X-Roadmap-ID";
    private static final String HEADER_PERMISSIONS = "X-Permissions";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        if (command == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(command)) {
            validateConnect(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(command)) {
            validateSubscribe(accessor);
        } else if (StompCommand.SEND.equals(command)) {
            validateSend(accessor);
        }

        return message;
    }

    private void validateConnect(StompHeaderAccessor accessor) {
        String userId = accessor.getFirstNativeHeader(HEADER_USER_ID);
        String roadmapId = accessor.getFirstNativeHeader(HEADER_ROADMAP_ID);
        String permissions = accessor.getFirstNativeHeader(HEADER_PERMISSIONS);

        if (userId == null || userId.isEmpty()) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }

        if (roadmapId == null || roadmapId.isEmpty()) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }

        if (permissions == null || permissions.isEmpty()) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }

        log.info("WebSocket CONNECT authorized: userId={}, roadmapId={}", userId, roadmapId);
    }

    private void validateSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        String permissions = accessor.getFirstNativeHeader(HEADER_PERMISSIONS);

        if (destination == null) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }

        if (destination.contains("/updates") || destination.contains("/topic/")) {
            PermissionValidator.requireView(permissions);
        }

        log.info("WebSocket SUBSCRIBE authorized: destination={}", destination);
    }

    private void validateSend(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        String permissions = accessor.getFirstNativeHeader(HEADER_PERMISSIONS);

        if (destination == null) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }

        if (destination.contains("/actions")) {
            log.info("WebSocket SEND action: destination={}", destination);
        }

        log.info("WebSocket SEND authorized: destination={}", destination);
    }
}



