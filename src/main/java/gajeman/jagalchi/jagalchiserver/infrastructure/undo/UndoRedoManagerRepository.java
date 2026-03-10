package gajeman.jagalchi.jagalchiserver.infrastructure.undo;

import gajeman.jagalchi.jagalchiserver.domain.undo.UndoRedoManager;

/**
 * UNDO/REDO 관리자 저장소
 * Redis에 사용자별 UNDO/REDO 상태를 저장
 */
public interface UndoRedoManagerRepository {
    /**
     * 사용자의 UNDO/REDO 관리자 조회
     *
     * @param roadmapId 로드맵 ID
     * @param userId 사용자 ID
     * @return UNDO/REDO 관리자 (없으면 새로 생성)
     */
    UndoRedoManager getManager(
            String roadmapId,
            Long userId
    );

    /**
     * UNDO/REDO 관리자 저장
     *
     * @param manager 저장할 관리자
     */
    void saveManager(
            UndoRedoManager manager
    );

    /**
     * UNDO/REDO 관리자 삭제 (초기화)
     *
     * @param roadmapId 로드맵 ID
     * @param userId 사용자 ID
     */
    void deleteManager(
            String roadmapId,
            Long userId
    );
}

