package gajeman.jagalchi.jagalchiserver.infrastructure.auth;

import gajeman.jagalchi.jagalchiserver.domain.auth.ActionContext;
import gajeman.jagalchi.jagalchiserver.domain.auth.UserRole;
import gajeman.jagalchi.jagalchiserver.global.exception.EditorException;
import gajeman.jagalchi.jagalchiserver.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Action 실행에 필요한 사용자 컨텍스트 추출
 * API Gateway에서 주입한 헤더로부터 ActionContext 생성
 * 
 * 헤더 명세:
 * - X-User-ID: 사용자 ID (Long)
 * - X-User-Role: 사용자 역할 (ADMIN, USER, GUEST)
 */
@Component
@Slf4j
public class ActionContextExtractor {

    public static final String HEADER_USER_ID = "X-User-ID";
    public static final String HEADER_USER_ROLE = "X-User-Role";

    /**
     * 헤더로부터 ActionContext 추출
     * 
     * @param userId X-User-ID 헤더 값
     * @param userRole X-User-Role 헤더 값
     * @return ActionContext 객체
     */
    public ActionContext extract(String userId, String userRole) {
        if (userId == null || userId.isBlank()) {
            log.debug("No userId provided, returning guest context");
            return ActionContext.guest();
        }

        try {
            Long parsedUserId = Long.parseLong(userId);
            UserRole parsedRole = UserRole.from(userRole);
            log.debug("Extracted ActionContext: userId={}, role={}", parsedUserId, parsedRole);
            return ActionContext.of(parsedUserId, parsedRole);
        } catch (NumberFormatException e) {
            log.error("Invalid userId format: {}", userId);
            throw new EditorException(ErrorCode.INVALID_USER_ID);
        }
    }

    /**
     * WebSocket 헤더 맵에서 ActionContext 추출
     * @param headers 헤더 맵
     * @return ActionContext
     */
    public ActionContext extractFromHeaders(Map<String, String> headers) {
        String userId = headers.get(HEADER_USER_ID);
        String userRole = headers.get(HEADER_USER_ROLE);
        return extract(userId, userRole);
    }
}

