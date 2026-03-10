package gajeman.jagalchi.jagalchiserver.application.event.usecase;

import gajeman.jagalchi.jagalchiserver.domain.event.Event;

import java.util.List;

/**
 * EVENT 조회 유즈케이스
 * logic.md: 클라이언트 재연결 시 놓친 EVENT 조회
 */
public interface GetEventsUseCase {
    /**
     * 특정 sequence 이후의 EVENT 목록 조회
     * 클라이언트가 재연결 시 사용
     *
     * @param roadmapId 로드맵 ID
     * @param sinceSequence 시작 sequence (이 값보다 큰 것만 조회)
     * @return EVENT 목록 (sequence 오름차순)
     */
    List<Event> getEventsSince(
            String roadmapId,
            Long sinceSequence
    );
}

