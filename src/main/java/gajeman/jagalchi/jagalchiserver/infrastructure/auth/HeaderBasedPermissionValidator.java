package gajeman.jagalchi.jagalchiserver.infrastructure.auth;

import gajeman.jagalchi.jagalchiserver.application.auth.PermissionValidator;
import gajeman.jagalchi.jagalchiserver.domain.auth.ActionContext;
import gajeman.jagalchi.jagalchiserver.domain.auth.UserRole;
import org.springframework.stereotype.Component;

/**
 * 헤더 기반 권한 검증 구현체
 *
 * 현재 구현:
 * - Role 기반 기본 권한 검증
 * - ADMIN: 모든 로드맵 편집/조회 가능
 * - USER: 본인 소유 로드맵만 편집 가능 (TODO: 소유자 확인 로직 추가)
 * - GUEST: 조회만 가능
 *
 * TODO: 향후 확장
 * - gRPC로 Permission 서비스 호출하여 세밀한 권한 확인
 * - 협업자 권한 확인 (roadmap_collaborators 테이블)
 * - 권한 결과 로컬 캐시 (TTL 5분)
 */
@Component
public class HeaderBasedPermissionValidator implements PermissionValidator {

    @Override
    public boolean canEditRoadmap(ActionContext context, Long roadmapId) {
        if (!context.isAuthenticated()) {
            return false;
        }

        // ADMIN은 모든 로드맵 편집 가능
        if (context.getRole() == UserRole.ADMIN) {
            return true;
        }

        // USER는 기본적으로 편집 가능 (Role 기반)
        // TODO: 실제 소유자/협업자 확인 로직 추가
        // TODO: gRPC로 Permission 서비스 호출하여 roadmapId 소유자 확인
        // 예시: permissionServiceClient.checkPermission(context.getUserId(), roadmapId, "EDIT")
        return context.getRole() == UserRole.USER;
    }

    @Override
    public boolean canViewRoadmap(ActionContext context, Long roadmapId) {
        // 모든 인증된 사용자는 조회 가능
        // TODO: 비공개 로드맵 처리 로직 추가
        return context.isAuthenticated();
    }
}
