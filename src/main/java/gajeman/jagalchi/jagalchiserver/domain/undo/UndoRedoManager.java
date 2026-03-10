package gajeman.jagalchi.jagalchiserver.domain.undo;

import gajeman.jagalchi.jagalchiserver.domain.action.ActionType;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

/**
 * UNDO/REDO 관리자
 * convention.md 섹션 12: Done Stack 기반 UNDO/REDO 관리
 *
 * 불변 객체 패턴:
 * - 모든 필드가 final
 * - 기본 생성자 없음 (빌더패턴으로만 생성)
 * - 상태 변경은 새 객체 생성
 */
@Getter
@Builder
public class UndoRedoManager implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 로드맵 ID
     */
    private final String roadmapId;

    /**
     * 사용자 ID
     */
    private final Long userId;

    /**
     * 처리 완료 스택 (사용자가 수행한 모든 행위들)
     */
    private final DoneStack doneStack;

    /**
     * UNDO 스택 (UNDO한 행위들을 임시 보관)
     */
    private final UndoStack undoStack;

    /**
     * 가장 최근 행위 타입
     * UNDO 후 REDO를 하기 위한 검증에 사용
     */
    private final ActionType lastActionType;

    /**
     * UNDO 가능 여부
     * 조건: doneStack이 비어있지 않음
     */
    public boolean canUndo() {
        return doneStack != null && !doneStack.isEmpty();
    }

    /**
     * REDO 가능 여부
     * 조건: undoStack이 비어있지 않음 AND 가장 최근 행위가 UNDO
     * 이것이 "가장 최근 행위가 UNDO가 아니면 REDO 불가능"을 구현
     */
    public boolean canRedo() {
        return undoStack != null && !undoStack.isEmpty()
            && lastActionType == ActionType.UNDO;
    }

    /**
     * 새로운 액션 기록
     * 규칙: doneStack에 push → undoStack 초기화 (새 분기 생성)
     *
     * @return 업데이트된 새 관리자 객체
     */
    public UndoRedoManager recordAction(
            CompletionItem item
    ) {
        DoneStack newDoneStack = (doneStack != null ? doneStack : DoneStack.empty())
            .push(item);

        return UndoRedoManager.builder()
                .roadmapId(this.roadmapId)
                .userId(this.userId)
                .doneStack(newDoneStack)
                .undoStack(UndoStack.empty())  // 새 분기 생성 (초기화)
                .lastActionType(item.actionType())
                .build();
    }

    /**
     * UNDO 실행
     * doneStack에서 pop → undoStack에 push
     *
     * @return UNDO된 CompletionItem (또는 null if canUndo = false)
     */
    public CompletionItem undo() {
        if (!canUndo()) {
            return null;
        }

        CompletionItem item = doneStack.peek();
        return item;
    }

    /**
     * REDO 실행
     * undoStack에서 pop → doneStack에 push
     * 조건: canRedo() = true
     *
     * @return REDO된 CompletionItem (또는 null if canRedo = false)
     */
    public CompletionItem redo() {
        if (!canRedo()) {
            return null;
        }

        CompletionItem item = undoStack.peek();
        return item;
    }

    /**
     * 초기화 (빈 상태로)
     * Builder 패턴과 호환
     */
    public void init() {
        // 불변 객체이므로 실제 초기화는 new 객체 생성으로 처리
        // 이 메서드는 마크/트리거 용도
    }

    /**
     * 스택 초기화
     *
     * @return 초기화된 새 관리자 객체
     */
    public UndoRedoManager reset() {
        return UndoRedoManager.builder()
                .roadmapId(this.roadmapId)
                .userId(this.userId)
                .doneStack(DoneStack.empty())
                .undoStack(UndoStack.empty())
                .lastActionType(null)
                .build();
    }
}

