package gajeman.jagalchi.jagalchiserver.application.snapshot.usecase;

import gajeman.jagalchi.jagalchiserver.domain.event.Snapshot;

/**
 * SNAPSHOT 생성 유즈케이스
 * logic.md: 초기 연결 시 전체 상태 제공
 */
public interface GetSnapshotUseCase {
    /**
     * 로드맵의 전체 상태 조회
     * 
     * @param roadmapId 로드맵 ID
     * @return SNAPSHOT (nodes, edges, sections 포함)
     */
    Snapshot getSnapshot(
            String roadmapId
    );
}

