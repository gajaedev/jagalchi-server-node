package gajeman.jagalchi.jagalchiserver.domain.action;

import gajeman.jagalchi.jagalchiserver.domain.event.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ACK 응답 DTO (logic.md 기준)
 * 명령이 처리 파이프라인에 정상적으로 진입했음을 의미
 *
 * 예시:
 * {
 *   "type": "ACK",
 *   "actionId": "act-401",
 *   "status": "ACCEPTED"
 * }
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionAck {
    /**
     * 응답 타입 (항상 ACK)
     */
    private EventType type;

    /**
     * 원본 액션 ID
     */
    private String actionId;

    /**
     * 처리 상태 (ACCEPTED / REJECTED)
     */
    private String status;

    /**
     * ACK 생성 factory method
     */
    public static ActionAck from(
            String actionId,
            String status
    ) {
        return ActionAck.builder()
                .type(EventType.ACK)
                .actionId(actionId)
                .status(status)
                .build();
    }
}



