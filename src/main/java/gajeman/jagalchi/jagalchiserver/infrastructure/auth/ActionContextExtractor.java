package gajeman.jagalchi.jagalchiserver.infrastructure.auth;

import gajeman.jagalchi.jagalchiserver.domain.auth.ActionContext;
import gajeman.jagalchi.jagalchiserver.domain.auth.UserRole;
import gajeman.jagalchi.jagalchiserver.global.exception.CustomException;
import gajeman.jagalchi.jagalchiserver.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 헤더에서 ActionContext를 추출하는 유틸리티
 * 헤더 규격:
 * - X-User-Id: 사용자 ID (Long)
 * - X-User-Role: 사용자 역할 (ADMIN, USER, GUEST)
 */
@Component
public class ActionContextExtractor {

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_ROLE = "X-User-Role";

    /**
     * HTTP 헤더에서 ActionContext 추출
     * @param userId X-User-Id 헤더 값
     * @param userRole X-User-Role 헤더 값
     * @return ActionContext
     */
    public ActionContext extract(String userId, String userRole) {
        if (userId == null || userId.isBlank()) {
            return ActionContext.guest();
        }

        try {
            Long parsedUserId = Long.parseLong(userId);
            UserRole parsedRole = UserRole.from(userRole);
            return ActionContext.of(parsedUserId, parsedRole);
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_USER_ID);
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

