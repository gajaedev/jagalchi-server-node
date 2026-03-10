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
    private final UserRole userRole;

    public static ActionContext of(
            Long userId,
            UserRole userRole
    ) {
        return ActionContext.builder()
                .userId(userId)
                .userRole(userRole)
                .build();
    }

    public static ActionContext guest() {
        return ActionContext.builder()
                .userId(null)
                .userRole(UserRole.GUEST)
                .build();
    }

    public boolean isAuthenticated() {
        return userId != null && userRole != UserRole.GUEST;
    }

    public boolean canEdit() {
        return isAuthenticated() && userRole.canEdit();
    }
}

