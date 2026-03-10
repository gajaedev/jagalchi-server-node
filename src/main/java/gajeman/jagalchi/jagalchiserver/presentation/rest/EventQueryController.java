package gajeman.jagalchi.jagalchiserver.presentation.rest;

import gajeman.jagalchi.jagalchiserver.application.event.usecase.GetEventsUseCase;
import gajeman.jagalchi.jagalchiserver.domain.event.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * EVENT 조회 REST API Controller
 * logic.md: 클라이언트 재연결 시 놓친 EVENT 조회
 */
@RestController
@RequestMapping("/api/roadmap/{roadmapId}/events")
@RequiredArgsConstructor
@Slf4j
public class EventQueryController {

    private final GetEventsUseCase getEventsUseCase;

    /**
     * 특정 sequence 이후의 EVENT 목록 조회
     *
     * GET /api/roadmap/{roadmapId}/events?since={sequence}
     *
     * 사용 시나리오:
     * 1. 클라이언트가 네트워크 끊김 후 재연결
     * 2. 마지막으로 받은 sequence 이후의 EVENT 조회
     * 3. 로컬 상태에 순서대로 적용
     *
     * @param roadmapId 로드맵 ID
     * @param since 시작 sequence (이 값보다 큰 것만 조회)
     * @return EVENT 목록 (sequence 오름차순)
     */
    @GetMapping
    public ResponseEntity<List<Event>> getEvents(
            @PathVariable String roadmapId,
            @RequestParam(defaultValue = "0") Long since
    ) {
        log.info("GET /api/roadmap/{}/events?since={}", roadmapId, since);

        List<Event> events = getEventsUseCase.getEventsSince(roadmapId, since);

        log.info("Returning {} events for roadmapId={} since sequence {}",
                events.size(), roadmapId, since);

        return ResponseEntity.ok(events);
    }
}

