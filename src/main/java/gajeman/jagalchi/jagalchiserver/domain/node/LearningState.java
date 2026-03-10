package gajeman.jagalchi.jagalchiserver.domain.node;

import lombok.Getter;

/**
 * 노드의 학습 상태
 * convention.md: Enum으로 고정 타입 관리
 *
 * 값:
 * - NOT_STARTED: 미시작 (기본값)
 * - IN_PROGRESS: 진행중
 * - COMPLETED: 완료
 */
@Getter
public enum LearningState {
    NOT_STARTED("미시작"),
    IN_PROGRESS("진행중"),
    COMPLETED("완료");

    private final String displayName;

    LearningState(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 문자열로부터 LearningState 조회
     */
    public static LearningState fromString(String value) {
        try {
            return LearningState.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NOT_STARTED;  // 기본값
        }
    }
}

