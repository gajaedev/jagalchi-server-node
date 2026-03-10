package gajeman.jagalchi.jagalchiserver.infrastructure.queue;

import gajeman.jagalchi.jagalchiserver.domain.queue.ActionQueueItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Redis 기반 Action 큐 저장소 구현
 * 요청 대기 큐와 완료 큐를 Redis에 저장
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class ActionQueueRepositoryImpl implements ActionQueueRepository {

    private final RedisTemplate<String, ActionQueueItem> redisTemplate;

    private static final String REQUEST_QUEUE_KEY_PREFIX = "action:queue:request:";
    private static final String COMPLETED_QUEUE_KEY_PREFIX = "action:queue:completed:";

    /**
     * 요청 큐 Key 생성
     */
    private String getRequestQueueKey(
            String roadmapId
    ) {
        return REQUEST_QUEUE_KEY_PREFIX + roadmapId;
    }

    /**
     * 완료 큐 Key 생성
     */
    private String getCompletedQueueKey(
            String roadmapId
    ) {
        return COMPLETED_QUEUE_KEY_PREFIX + roadmapId;
    }

    @Override
    public void enqueueRequest(
            ActionQueueItem item
    ) {
        String key = getRequestQueueKey(item.getRoadmapId());
        redisTemplate.opsForList().rightPush(key, item);
        log.debug("Action enqueued to request queue: roadmapId={}, actionId={}",
                item.getRoadmapId(), item.getActionId());
    }

    @Override
    public Optional<ActionQueueItem> dequeueRequest(
            String roadmapId
    ) {
        String key = getRequestQueueKey(roadmapId);
        ActionQueueItem item = redisTemplate.opsForList().leftPop(key);

        if (item != null) {
            log.debug("Action dequeued from request queue: roadmapId={}, actionId={}",
                    roadmapId, item.getActionId());
        }

        return Optional.ofNullable(item);
    }

    @Override
    public List<ActionQueueItem> getRequestQueue(
            String roadmapId
    ) {
        String key = getRequestQueueKey(roadmapId);
        List<ActionQueueItem> items = redisTemplate.opsForList().range(key, 0, -1);
        return items != null ? items : List.of();
    }

    @Override
    public void enqueueCompleted(
            ActionQueueItem item
    ) {
        String key = getCompletedQueueKey(item.getRoadmapId());
        redisTemplate.opsForList().rightPush(key, item);
        log.debug("Action moved to completed queue: roadmapId={}, actionId={}",
                item.getRoadmapId(), item.getActionId());
    }

    @Override
    public List<ActionQueueItem> getCompletedQueue(
            String roadmapId
    ) {
        String key = getCompletedQueueKey(roadmapId);
        List<ActionQueueItem> items = redisTemplate.opsForList().range(key, 0, -1);
        return items != null ? items : List.of();
    }

    @Override
    public void removeCompleted(
            String queueId
    ) {
        // Redis List에서 특정 항목을 ID로 찾아서 제거하기는 어려움
        // 실무에서는 로드맵 ID 단위로 처리하거나 Set을 사용하는 것이 좋음
        log.debug("Remove completed item: queueId={}", queueId);
    }

    @Override
    public void clearRequestQueue(
            String roadmapId
    ) {
        String key = getRequestQueueKey(roadmapId);
        redisTemplate.delete(key);
        log.info("Request queue cleared: roadmapId={}", roadmapId);
    }
}

