package gajeman.jagalchi.jagalchiserver.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Event DTO (logic.md 기준)
 * 서버가 상태 확정 후 브로드캐스트하는 이벤트
 *
 * 예시:
 * {
 *   "type": "EVENT",
 *   "eventId": "evt-301",
 *   "sequence": 43,
 *   "payload": {
 *     "type": "MOVE",
 *     "target": {
 *       "type": "NODE",
 *       "object": "node-1"
 *     },
 *     "state": {
 *       "x": 300,
 *       "y": 200
 *     }
 *   }
 * }
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    /**
     * 이벤트 타입 (항상 EVENT)
     */
    private EventType type;

    /**
     * 이벤트 고유 ID (서버 생성)
     */
    private String eventId;

    /**
     * 시퀀스 번호 (순서 보장용)
     */
    private Long sequence;

    /**
     * 이벤트 페이로드 (서버가 확정한 최종 상태)
     */
    private Map<String, Object> payload;

    /**
     * Event 생성 factory method
     */
    public static Event from(
            String eventId,
            Long sequence,
            Map<String, Object> payload
    ) {
        return Event.builder()
                .type(EventType.EVENT)
                .eventId(eventId)
                .sequence(sequence)
                .payload(payload)
                .build();
    }
}

