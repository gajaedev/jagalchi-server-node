package gajeman.jagalchi.jagalchiserver.infrastructure.queue;

import gajeman.jagalchi.jagalchiserver.domain.queue.ActionQueueItem;

import java.util.List;
import java.util.Optional;

/**
 * Redis 기반 Action 큐 저장소
 * convention.md: 요청 대기 큐와 완료 큐 관리
 */
public interface ActionQueueRepository {
    /**
     * 요청 큐에 액션 추가
     *
     * @param item 액션 큐 항목
     */
    void enqueueRequest(
            ActionQueueItem item
    );

    /**
     * 요청 큐에서 액션 조회 및 제거
     *
     * @param roadmapId 로드맵 ID
     * @return 액션 큐 항목 (없으면 empty)
     */
    Optional<ActionQueueItem> dequeueRequest(
            String roadmapId
    );

    /**
     * 요청 큐의 모든 항목 조회
     *
     * @param roadmapId 로드맵 ID
     * @return 큐 항목 목록
     */
    List<ActionQueueItem> getRequestQueue(
            String roadmapId
    );

    /**
     * 완료 큐에 항목 추가
     *
     * @param item 완료된 액션 항목
     */
    void enqueueCompleted(
            ActionQueueItem item
    );

    /**
     * 완료 큐에서 항목 조회
     *
     * @param roadmapId 로드맵 ID
     * @return 완료된 항목 목록
     */
    List<ActionQueueItem> getCompletedQueue(
            String roadmapId
    );

    /**
     * 완료 큐에서 항목 제거
     *
     * @param queueId 큐 항목 ID
     */
    void removeCompleted(
            String queueId
    );

    /**
     * 요청 큐 비우기
     *
     * @param roadmapId 로드맵 ID
     */
    void clearRequestQueue(
            String roadmapId
    );
}

