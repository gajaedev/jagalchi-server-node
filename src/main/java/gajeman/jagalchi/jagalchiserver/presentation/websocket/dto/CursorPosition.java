package gajeman.jagalchi.jagalchiserver.presentation.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 커서 위치 DTO
 * 실시간 협업을 위한 사용자 커서 위치 추적
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorPosition {
    /**
     * 사용자 ID
     */
    private Long userId;

    /**
     * 사용자 이름 (표시용)
     */
    private String userName;

    /**
     * X 좌표
     */
    private Double x;

    /**
     * Y 좌표
     */
    private Double y;

    /**
     * 타임스탬프 (밀리초)
     */
    private Long timestamp;

    /**
     * 커서 상태 (NORMAL, DRAGGING, SELECTING)
     */
    private CursorState state;

    /**
     * 선택/드래그 중인 요소 ID (선택사항)
     */
    private String targetId;
}
