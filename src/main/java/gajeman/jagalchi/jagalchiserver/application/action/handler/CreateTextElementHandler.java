package gajeman.jagalchi.jagalchiserver.application.action.handler;

import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionData;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionPayload;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionTarget;
import gajeman.jagalchi.jagalchiserver.domain.event.Event;
import gajeman.jagalchi.jagalchiserver.domain.node.RoadmapTextElement;
import gajeman.jagalchi.jagalchiserver.domain.payload.TargetType;
import gajeman.jagalchi.jagalchiserver.global.exception.ActionValidationException;
import gajeman.jagalchi.jagalchiserver.infrastructure.persistence.RoadmapTextElementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 텍스트 요소 생성 핸들러
 * logic.md: CREATE 액션 (TEXT 대상)
 *
 * 캔버스에 독립 텍스트 요소 생성
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CreateTextElementHandler implements ActionHandler {

    private final RoadmapTextElementRepository roadmapTextElementRepository;

    @Override
    public Event handle(
            Action action
    ) {
        // 1. payload 검증
        validatePayload(action);

        ActionPayload payload = action.getPayload();
        ActionData data = payload.getData();

        // 2. 필수 데이터 추출
        String content = data.getTextContent();
        Float x = data.getX();
        Float y = data.getY();

        if (content == null || content.trim().isEmpty()) {
            throw new ActionValidationException(
                    "INVALID_TEXT_CONTENT",
                    "텍스트 내용이 비어있습니다"
            );
        }

        if (x == null || y == null) {
            throw new ActionValidationException(
                    "INVALID_TEXT_POSITION",
                    "텍스트 위치(x, y)가 필요합니다"
            );
        }

        // 3. unitId 추출
        Long unitId = Long.parseLong(action.getRoadmap());

        // 4. 텍스트 요소 생성
        RoadmapTextElement textElement = RoadmapTextElement.builder()
                .unitId(unitId)
                .content(content)
                .x(x)
                .y(y)
                .fontSize(data.getFontSize() != null ? data.getFontSize() : 16)
                .color(data.getTextColor() != null ? data.getTextColor() : "#000000")
                .fontWeight(data.getFontWeight() != null ? data.getFontWeight() : "normal")
                .textAlign(data.getTextAlign() != null ? data.getTextAlign() : "left")
                .styleData(data.getStyleData())
                .build();

        RoadmapTextElement createdTextElement = roadmapTextElementRepository.save(textElement);

        log.info("Text element created: id={}, unitId={}, content={}",
                createdTextElement.getId(), unitId, content);

        // 5. Event 생성
        return buildEvent(createdTextElement);
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
        if (target == null || target.getType() != TargetType.TEXT) {
            throw new ActionValidationException(
                    "INVALID_TARGET",
                    "대상이 텍스트 요소(TEXT)이어야 합니다"
            );
        }
    }

    /**
     * Event 생성
     */
    private Event buildEvent(
            RoadmapTextElement createdTextElement
    ) {
        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("type", "CREATE");

        // target 정보
        Map<String, Object> target = new HashMap<>();
        target.put("type", "TEXT");
        target.put("object", createdTextElement.getId().toString());
        eventPayload.put("target", target);

        // 생성된 텍스트 요소 상태
        eventPayload.put("state", createdTextElement.toDto());

        // Event ID 생성
        String eventId = "evt-" + UUID.randomUUID().toString().substring(0, 8);
        Long sequence = System.currentTimeMillis();

        return Event.from(eventId, sequence, eventPayload);
    }
}

