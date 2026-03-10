package gajeman.jagalchi.jagalchiserver.presentation.interceptor;

import gajeman.jagalchi.jagalchiserver.global.exception.EditorException;
import gajeman.jagalchi.jagalchiserver.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 권한 검증 인터셉터
 * convention.md: 권한 검증 분리
 * AUTH_HEADER_DESIGN.md: API Gateway 기반 헤더 검증
 *
 * API Gateway에서 해체한 헤더 기반 권한 검증
 * - X-User-ID: 사용자 ID
 * - X-Roadmap-ID: 로드맵 ID
 * - X-Permissions: 권한 목록 (쉼표 구분)
 */
@Component
@Slf4j
public class AuthenticationInterceptor implements HandlerInterceptor {

    private static final String HEADER_USER_ID = "X-User-ID";
    private static final String HEADER_ROADMAP_ID = "X-Roadmap-ID";
    private static final String HEADER_PERMISSIONS = "X-Permissions";

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {
        // 1. 인증 헤더 검증
        validateAuthHeaders(request);

        // 2. 요청에 사용자 정보 저장 (컨트롤러에서 접근 가능)
        String userId = request.getHeader(HEADER_USER_ID);
        String roadmapId = request.getHeader(HEADER_ROADMAP_ID);
        String permissions = request.getHeader(HEADER_PERMISSIONS);

        request.setAttribute("userId", userId);
        request.setAttribute("roadmapId", roadmapId);
        request.setAttribute("permissions", permissions);

        log.info("Authentication successful: userId={}, roadmapId={}, permissions={}",
                userId, roadmapId, permissions);

        return true;
    }

    /**
     * 인증 헤더 검증
     */
    private void validateAuthHeaders(
            HttpServletRequest request
    ) {
        String userId = request.getHeader(HEADER_USER_ID);
        String roadmapId = request.getHeader(HEADER_ROADMAP_ID);
        String permissions = request.getHeader(HEADER_PERMISSIONS);

        // 필수 헤더 확인
        if (userId == null || userId.isEmpty()) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }

        if (roadmapId == null || roadmapId.isEmpty()) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }

        if (permissions == null || permissions.isEmpty()) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }

        log.debug("Auth headers validated: userId={}, roadmapId={}", userId, roadmapId);
    }
}

