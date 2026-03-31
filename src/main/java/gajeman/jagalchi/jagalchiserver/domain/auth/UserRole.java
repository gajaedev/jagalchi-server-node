package gajeman.jagalchi.jagalchiserver.domain.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 역할 (Role)
 * 
 * API Gateway에서 User 모듈의 role을 다음과 같이 매핑:
 * - STUDENT → USER
 * - TEACHER → ADMIN
 * - ADMIN → ADMIN
 */
@Getter
@RequiredArgsConstructor
public enum UserRole {
    ADMIN("ADMIN"),
    USER("USER"),
    GUEST("GUEST");

    private final String value;

    /**
     * 문자열로부터 UserRole 변환
     * User 모듈 role 매핑 지원 (STUDENT → USER, TEACHER → ADMIN)
     */
    public static UserRole from(String value) {
        if (value == null || value.isBlank()) {
            return GUEST;
        }
        
        // User 모듈 role 매핑
        String normalized = value.toUpperCase();
        return switch (normalized) {
            case "ADMIN", "TEACHER" -> ADMIN;
            case "USER", "STUDENT" -> USER;
            case "GUEST" -> GUEST;
            default -> GUEST;  // 알 수 없는 role은 GUEST
        };
    }

    public boolean canEdit() {
        return this == ADMIN || this == USER;
    }
}

