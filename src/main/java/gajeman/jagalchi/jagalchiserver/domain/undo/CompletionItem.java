package gajeman.jagalchi.jagalchiserver.domain.undo;

import gajeman.jagalchi.jagalchiserver.domain.action.ActionType;

import java.io.Serializable;
import java.util.Map;

/**
 * 처리 완료 항목 (Completion Item)
 * 사용자가 수행한 하나의 행위를 나타냄
 *
 * Record 사용 (convention.md 섹션 11):
 * - 순수 DTO는 Record로 정의
 * - 자동으로 불변성 보장 (final 필드)
 * - Serializable 구현 가능
 *
 * @param actionType 액션 타입
 * @param previousState 액션이 수행되기 전의 상태
 * @param resultingState 액션 수행 후의 상태
 * @param timestamp 행위 수행 시각
 * @param actionId 액션 ID (참조용)
 * @param entityId 대상 엔티티 ID
 * @param entityType 대상 엔티티 타입 (NODE, EDGE, SECTION)
 */
public record CompletionItem(
    ActionType actionType,
    Map<String, Object> previousState,
    Map<String, Object> resultingState,
    Long timestamp,
    String actionId,
    Long entityId,
    String entityType
) implements Serializable {
    private static final long serialVersionUID = 1L;
}

