package gajeman.jagalchi.jagalchiserver.application.auth;

import gajeman.jagalchi.jagalchiserver.domain.auth.ActionContext;

/**
 * 권한 검증 인터페이스
 * TODO: 향후 gRPC 기반 Permission 서비스 연동 시 구현체 교체 가능
 */
public interface PermissionValidator {

    /**
     * 로드맵 편집 권한 확인
     * @param context 액션 컨텍스트 (userId, role)
     * @param roadmapId 대상 로드맵 ID
     * @return 편집 가능 여부
     */
    boolean canEditRoadmap(ActionContext context, Long roadmapId);

    /**
     * 로드맵 조회 권한 확인
     * @param context 액션 컨텍스트 (userId, role)
     * @param roadmapId 대상 로드맵 ID
     * @return 조회 가능 여부
     */
    boolean canViewRoadmap(ActionContext context, Long roadmapId);
}

