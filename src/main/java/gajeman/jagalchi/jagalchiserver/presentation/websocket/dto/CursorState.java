package gajeman.jagalchi.jagalchiserver.presentation.websocket.dto;

/**
 * 커서 상태
 */
public enum CursorState {
    NORMAL,      // 일반 커서
    DRAGGING,    // 드래그 중
    SELECTING,   // 선택 중
    EDITING      // 편집 중
}
