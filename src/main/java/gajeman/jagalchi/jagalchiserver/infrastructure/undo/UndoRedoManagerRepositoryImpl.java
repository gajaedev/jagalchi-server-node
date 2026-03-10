package gajeman.jagalchi.jagalchiserver.infrastructure.undo;

import gajeman.jagalchi.jagalchiserver.domain.undo.UndoRedoManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

/**
 * Redis 기반 UNDO/REDO 관리자 저장소 구현
 * convention.md 섹션 12: Redis에 사용자별 UNDO/REDO 상태 저장
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class UndoRedoManagerRepositoryImpl implements UndoRedoManagerRepository {

    private final RedisTemplate<String, UndoRedoManager> redisTemplate;

    private static final String MANAGER_KEY_PREFIX = "undo:redo:manager:";
    private static final long EXPIRATION_HOURS = 24;

    /**
     * Redis Key 생성
     */
    private String getManagerKey(
            String roadmapId,
            Long userId
    ) {
        return MANAGER_KEY_PREFIX + roadmapId + ":" + userId;
    }

    @Override
    public UndoRedoManager getManager(
            String roadmapId,
            Long userId
    ) {
        String key = getManagerKey(roadmapId, userId);
        UndoRedoManager manager = redisTemplate.opsForValue().get(key);

        if (manager == null) {
            // 없으면 새 관리자 생성
            manager = UndoRedoManager.builder()
                    .roadmapId(roadmapId)
                    .userId(userId)
                    .build();
            manager.init();
        }

        log.debug("UNDO/REDO manager retrieved: roadmapId={}, userId={}, canUndo={}, canRedo={}",
                roadmapId, userId, manager.canUndo(), manager.canRedo());

        return manager;
    }

    @Override
    public void saveManager(
            UndoRedoManager manager
    ) {
        String key = getManagerKey(manager.getRoadmapId(), manager.getUserId());

        // Redis에 저장 (24시간 TTL)
        redisTemplate.opsForValue().set(
                key,
                manager,
                EXPIRATION_HOURS,
                TimeUnit.HOURS
        );

        log.debug("UNDO/REDO manager saved: roadmapId={}, userId={}, doneStackSize={}, undoStackSize={}, lastActionType={}",
                manager.getRoadmapId(),
                manager.getUserId(),
                manager.getDoneStack() != null ? manager.getDoneStack().size() : 0,
                manager.getUndoStack() != null ? manager.getUndoStack().size() : 0,
                manager.getLastActionType()
        );
    }

    @Override
    public void deleteManager(
            String roadmapId,
            Long userId
    ) {
        String key = getManagerKey(roadmapId, userId);
        redisTemplate.delete(key);

        log.info("UNDO/REDO manager deleted: roadmapId={}, userId={}", roadmapId, userId);
    }
}

