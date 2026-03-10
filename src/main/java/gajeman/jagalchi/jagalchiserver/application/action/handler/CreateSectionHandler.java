package gajeman.jagalchi.jagalchiserver.application.action.handler;

import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionData;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionPayload;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionTarget;
import gajeman.jagalchi.jagalchiserver.domain.event.Event;
import gajeman.jagalchi.jagalchiserver.domain.node.RoadmapSection;
import gajeman.jagalchi.jagalchiserver.domain.payload.TargetType;
import gajeman.jagalchi.jagalchiserver.global.exception.ActionValidationException;
import gajeman.jagalchi.jagalchiserver.infrastructure.persistence.RoadmapSectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 섹션 생성 핸들러
 * logic.md: CREATE 액션 (SECTION 대상)
 * SECTION_DESIGN.md: 섹션 설계 기준
 *
 * 캔버스에 노드 그룹화 영역 생성
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CreateSectionHandler implements ActionHandler {

    private final RoadmapSectionRepository roadmapSectionRepository;

    @Override
    public Event handle(
            Action action
    ) {
        // 1. payload 검증
        validatePayload(action);

        ActionPayload payload = action.getPayload();
        ActionData data = payload.getData();

        // 2. 필수 데이터 추출
        String name = data.getSectionName();
        Float x = data.getX();
        Float y = data.getY();
        Float width = data.getSectionWidth();
        Float height = data.getSectionHeight();

        // 3. 필수 필드 검증
        if (name == null || name.trim().isEmpty()) {
            throw new ActionValidationException(
                    "INVALID_SECTION_NAME",
                    "섹션명은 필수입니다"
            );
        }

        if (name.length() > 100) {
            throw new ActionValidationException(
                    "SECTION_NAME_TOO_LONG",
                    "섹션명은 100자 이하여야 합니다"
            );
        }

        if (x == null || y == null) {
            throw new ActionValidationException(
                    "INVALID_SECTION_POSITION",
                    "섹션 위치(x, y)가 필요합니다"
            );
        }

        if (x < 0 || y < 0) {
            throw new ActionValidationException(
                    "INVALID_SECTION_POSITION",
                    "섹션 위치는 0 이상이어야 합니다"
            );
        }

        if (width == null || height == null) {
            throw new ActionValidationException(
                    "INVALID_SECTION_SIZE",
                    "섹션 크기(width, height)가 필요합니다"
            );
        }

        if (width <= 0 || height <= 0) {
            throw new ActionValidationException(
                    "INVALID_SECTION_SIZE",
                    "섹션 크기는 0보다 커야 합니다"
            );
        }

        // 4. unitId 추출
        Long unitId = Long.parseLong(action.getRoadmap());

        // 5. 섹션 생성
        RoadmapSection section = RoadmapSection.builder()
                .unitId(unitId)
                .name(name)
                .x(x)
                .y(y)
                .width(width)
                .height(height)
                .color(data.getSectionColor() != null ? data.getSectionColor() : "#e8f4f8")
                .description(data.getSectionDescription())
                .build();

        RoadmapSection createdSection = roadmapSectionRepository.save(section);

        log.info("Section created: id={}, unitId={}, name={}, position=({}, {})",
                createdSection.getId(), unitId, name, x, y);

        // 6. Event 생성
        return buildEvent(createdSection);
    }

    /**
     * Payload 검증
     */
    private void validatePayload(
            Action action
    ) {
        ActionPayload payload = action.getPayload();
        if (payload == null) {
            throw new ActionValidationException(
                    "INVALID_PAYLOAD",
                    "payload가 없습니다"
            );
        }

        ActionTarget target = payload.getTarget();
        if (target == null || target.getType() != TargetType.SECTION) {
            throw new ActionValidationException(
                    "INVALID_TARGET",
                    "대상이 섹션(SECTION)이어야 합니다"
            );
        }
    }

    /**
     * Event 생성
     */
    private Event buildEvent(
            RoadmapSection createdSection
    ) {
        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("type", "CREATE");

        // target 정보
        Map<String, Object> target = new HashMap<>();
        target.put("type", "SECTION");
        target.put("object", createdSection.getId().toString());
        eventPayload.put("target", target);

        // 생성된 섹션 상태
        eventPayload.put("state", createdSection.toDto());

        // Event ID 생성
        String eventId = "evt-" + UUID.randomUUID().toString().substring(0, 8);
        Long sequence = System.currentTimeMillis();

        return Event.from(eventId, sequence, eventPayload);
    }
}

