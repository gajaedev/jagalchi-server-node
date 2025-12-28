package gajeman.jagalchi.jagalchiserver.application.action;

import gajeman.jagalchi.jagalchiserver.application.auth.PermissionValidator;
import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.auth.ActionContext;
import gajeman.jagalchi.jagalchiserver.global.exception.CustomException;
import gajeman.jagalchi.jagalchiserver.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Action 처리 전 공통 검증 파이프라인
 *
 * logic.md 기준 서버 공통 처리:
 * 1. actionId 중복 여부 확인
 * 2. 로드맵 존재 확인
 * 3. 사용자 편집 권한 확인
 * 4. 로드맵 편집 가능 상태 확인
 */
@Component
@RequiredArgsConstructor
public class ActionPipelineValidator {

    private final PermissionValidator permissionValidator;

    // TODO: Redis 기반 분산 중복 체크로 교체 필요
    // 현재는 단일 인스턴스 기준 메모리 기반 중복 체크
    private final Set<String> processedActionIds = ConcurrentHashMap.newKeySet();

    /**
     * Action 처리 전 공통 검증 수행
     * @param action 처리할 Action
     * @param context 인증/인가 컨텍스트
     */
    public void validate(Action action, ActionContext context) {
        validateActionIdNotDuplicate(action.getActionId());
        validateAuthenticated(context);
        validateEditPermission(context, action.getRoadmapId());
        // TODO: 로드맵 존재 여부 확인 (Roadmap 서비스 연동 필요)
        // TODO: 로드맵 편집 가능 상태 확인 (잠금 상태 등)
    }

    /**
     * 1. actionId 중복 여부 확인
     */
    private void validateActionIdNotDuplicate(String actionId) {
        if (actionId == null || actionId.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        if (!processedActionIds.add(actionId)) {
            throw new CustomException(ErrorCode.DUPLICATE_ACTION);
        }
        // TODO: 메모리 관리 - 일정 시간 후 actionId 제거 (TTL 적용)
        // TODO: Redis SET으로 교체하여 분산 환경 지원
    }

    /**
     * 2. 인증 여부 확인
     */
    private void validateAuthenticated(ActionContext context) {
        if (!context.isAuthenticated()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
    }

    /**
     * 3. 편집 권한 확인
     */
    private void validateEditPermission(ActionContext context, Long roadmapId) {
        if (!permissionValidator.canEditRoadmap(context, roadmapId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    /**
     * 처리 완료된 actionId 기록 (성공 시)
     * 실패 시에는 재시도 가능하도록 제거
     */
    public void markAsProcessed(String actionId) {
        processedActionIds.add(actionId);
    }

    /**
     * 처리 실패 시 actionId 제거 (재시도 허용)
     */
    public void removeFromProcessed(String actionId) {
        processedActionIds.remove(actionId);
    }
}

