package gajeman.jagalchi.jagalchiserver.application.event.service;

import gajeman.jagalchi.jagalchiserver.application.event.usecase.GetEventsUseCase;
import gajeman.jagalchi.jagalchiserver.domain.event.Event;
import gajeman.jagalchi.jagalchiserver.domain.event.EventType;
import gajeman.jagalchi.jagalchiserver.domain.history.ActionHistory;
import gajeman.jagalchi.jagalchiserver.infrastructure.persistence.ActionHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * EVENT 조회 유즈케이스 구현체
 * logic.md: 클라이언트 재연결 시 놓친 EVENT 조회
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetEventsUseCaseImpl implements GetEventsUseCase {

    private final ActionHistoryRepository actionHistoryRepository;

    /**
     * 특정 sequence 이후의 EVENT 목록 조회
     *
     * @param roadmapId 로드맵 ID
     * @param sinceSequence 시작 sequence (이 값보다 큰 것만 조회)
     * @return EVENT 목록 (sequence 오름차순)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Event> getEventsSince(
            String roadmapId,
            Long sinceSequence
    ) {
        log.info("Fetching events since sequence: roadmapId={}, sinceSequence={}",
                roadmapId, sinceSequence);

        // 1. ActionHistory에서 조회
        List<ActionHistory> histories = actionHistoryRepository
                .findByRoadmapIdAndSequenceGreaterThanOrderBySequenceAsc(roadmapId, sinceSequence);

        // 2. ActionHistory → Event 변환
        List<Event> events = histories.stream()
                .map(this::toEvent)
                .toList();

        log.info("Found {} events since sequence {}", events.size(), sinceSequence);

        return events;
    }

    /**
     * ActionHistory를 Event로 변환
     *
     * @param history ActionHistory
     * @return Event
     */
    private Event toEvent(
            ActionHistory history
    ) {
        return Event.builder()
                .type(EventType.EVENT)
                .eventId(history.getEventId())
                .sequence(history.getSequence())
                .payload(history.getEventPayload())
                .build();
    }
}

