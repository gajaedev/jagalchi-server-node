package gajeman.jagalchi.jagalchiserver.domain.action;

/**
 * Action 타입 (logic.md 기준)
 * 클라이언트가 서버에 전송하는 명령 타입
 */
public enum ActionType {
    CREATE,  // 생성
    EDIT,    // 수정 (MOVE, SCALE, LOCK, COPY 등 모두 포함)
    DELETE,  // 삭제
    UNDO,    // 실행 취소
    REDO     // 다시 실행
}

