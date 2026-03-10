package gajeman.jagalchi.jagalchiserver.domain.undo;

import lombok.Getter;

import java.io.Serializable;
import java.util.Stack;

/**
 * 처리 완료 스택 (Done Stack)
 * 사용자가 수행한 모든 행위들을 보관
 * convention.md 섹션 12: Done Stack 기반 UNDO/REDO
 *
 * 불변 객체 패턴:
 * - final 필드
 * - 상태 변경은 새 객체 반환
 */
@Getter
public class DoneStack implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 불변 스택
     */
    private final Stack<CompletionItem> items;

    /**
     * 생성자는 private (빌더 패턴 또는 정적 팩토리 메서드 사용)
     */
    private DoneStack(Stack<CompletionItem> items) {
        this.items = items;
    }

    /**
     * 빈 스택 생성
     */
    public static DoneStack empty() {
        return new DoneStack(new Stack<>());
    }

    /**
     * 새로운 항목을 추가한 새 스택 반환 (불변 패턴)
     */
    public DoneStack push(
            CompletionItem item
    ) {
        Stack<CompletionItem> newItems = (Stack<CompletionItem>) this.items.clone();
        newItems.push(item);
        return new DoneStack(newItems);
    }

    /**
     * 가장 위의 항목을 제거한 새 스택 반환 (불변 패턴)
     */
    public DoneStack pop() {
        if (isEmpty()) {
            return this;
        }

        Stack<CompletionItem> newItems = (Stack<CompletionItem>) this.items.clone();
        newItems.pop();
        return new DoneStack(newItems);
    }

    /**
     * 가장 위의 항목만 조회 (변경 없음)
     */
    public CompletionItem peek() {
        return isEmpty() ? null : items.peek();
    }

    /**
     * 스택이 비어있는지 확인
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * 스택 크기
     */
    public int size() {
        return items.size();
    }
}

