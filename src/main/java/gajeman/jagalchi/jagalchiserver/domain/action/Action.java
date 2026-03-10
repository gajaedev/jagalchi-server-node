package gajeman.jagalchi.jagalchiserver.domain.action;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Action 요청 DTO (logic.md 기준)
 * 클라이언트가 서버에 전송하는 명령
 *
 * 예시:
 * {
 *   "actionId": "act-123",
 *   "roadmap": "roadmap-id",
 *   "action": "CREATE",
 *   "payload": {
 *     "type": "INFO",
 *     "target": {
 *       "type": "NODE",
 *       "object": "temp-id"
 *     },
 *     "data": {
 *       "label": "새 노드",
 *       "x": 120,
 *       "y": 300
 *     }
 *   }
 * }
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Action {
    /**
     * 액션 고유 ID (클라이언트 생성, ACK 응답에 사용)
     */
    private String actionId;

    /**
     * 로드맵 ID (외부 서비스의 roadmap ID)
     */
    private String roadmap;

    /**
     * 액션 타입 (CREATE/EDIT/DELETE/UNDO/REDO)
     */
    private ActionType action;

    /**
     * 액션 페이로드 (타입별로 구조가 다름)
     * - CREATE/EDIT/DELETE: type, target, data 포함
     * - UNDO/REDO: payload 불필요 (null 가능)
     */
    private ActionPayload payload;
}



