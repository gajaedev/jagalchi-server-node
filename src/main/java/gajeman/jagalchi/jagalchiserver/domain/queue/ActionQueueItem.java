package gajeman.jagalchi.jagalchiserver.domain.queue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Redis 요청 대기 큐 항목
 * 처리할 Action을 저장
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionQueueItem implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 큐 항목 ID (고유값)
     */
    private String queueId;

    /**
     * 로드맵 ID
     */
    private String roadmapId;

    /**
     * 사용자 ID
     */
    private Long userId;

    /**
     * 액션 ID
     */
    private String actionId;

    /**
     * 액션 타입
     */
    private String actionType;

    /**
     * 액션 페이로드 (JSON 문자열)
     */
    private String payload;

    /**
     * 큐 진입 시각 (Unix timestamp)
     */
    private Long enqueuedAt;

    /**
     * 우선순위 (0-100, 클수록 높음)
     */
    private Integer priority;
}

