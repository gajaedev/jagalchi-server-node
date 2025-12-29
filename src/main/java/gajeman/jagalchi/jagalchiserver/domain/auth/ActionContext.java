package gajeman.jagalchi.jagalchiserver.domain.auth;

import lombok.Builder;
import lombok.Getter;

/**
 * Action 처리 시 필요한 인증/인가 컨텍스트
 * 헤더에서 추출한 유저 정보를 담는다.
 */
@Getter
@Builder
public class ActionContext {
    private final Long userId;
    private final UserRole role;

    public static ActionContext of(Long userId, UserRole role) {
        return ActionContext.builder()
                .userId(userId)
                .role(role)
                .build();
    }

    public static ActionContext guest() {
        return ActionContext.builder()
                .userId(null)
                .role(UserRole.GUEST)
                .build();
    }

    public boolean isAuthenticated() {
        return userId != null && role != UserRole.GUEST;
    }

    public boolean canEdit() {
        return isAuthenticated() && role.canEdit();
    }
}

