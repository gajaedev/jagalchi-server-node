package gajeman.jagalchi.jagalchiserver.application.action.handler;

import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionData;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionPayload;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionTarget;
import gajeman.jagalchi.jagalchiserver.domain.event.Event;
import gajeman.jagalchi.jagalchiserver.domain.node.RoadmapTextElement;
import gajeman.jagalchi.jagalchiserver.domain.payload.TargetType;
import gajeman.jagalchi.jagalchiserver.global.exception.ActionValidationException;
import gajeman.jagalchi.jagalchiserver.global.exception.EditorException;
import gajeman.jagalchi.jagalchiserver.global.exception.ErrorCode;
import gajeman.jagalchi.jagalchiserver.infrastructure.persistence.RoadmapTextElementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 텍스트 요소 수정 핸들러
 * logic.md: EDIT 액션 (TEXT 대상)
 *
 * 텍스트 요소의 내용, 스타일, 위치 수정
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EditTextElementHandler implements ActionHandler {

    private final RoadmapTextElementRepository roadmapTextElementRepository;

    @Override
    public Event handle(Action action) {
        validatePayload(action);

        ActionPayload payload = action.getPayload();
        ActionTarget target = payload.getTarget();
        ActionData data = payload.getData();

        String textIdStr = target.getObject();
        if (textIdStr == null) {
            throw new ActionValidationException("INVALID_TEXT_ID", "텍스트 요소 ID가 없습니다");
        }

        Long textId = Long.parseLong(textIdStr);
        RoadmapTextElement textElement = roadmapTextElementRepository.findById(textId)
                .orElseThrow(() -> new EditorException(ErrorCode.INVALID_INPUT));

        // 데이터 검증
        validateData(data);

        // Builder 패턴으로 업데이트
        RoadmapTextElement updated = updateTextElementProperties(textElement, data);
        RoadmapTextElement savedElement = roadmapTextElementRepository.save(updated);

        log.info("Text element updated: textId={}, content={}", textId, savedElement.getContent());

        return buildEvent(savedElement);
    }

    /**
     * 데이터 검증
     */
    private void validateData(ActionData data) {
        if (data == null) {
            return;
        }

        if (data.getFontSize() != null && data.getFontSize() <= 0) {
            throw new ActionValidationException("INVALID_FONT_SIZE", "폰트 크기는 0보다 커야 합니다");
        }

        if (data.getTextColor() != null) {
            validateColor(data.getTextColor());
        }

        if (data.getFontWeight() != null) {
            validateFontWeight(data.getFontWeight());
        }

        if (data.getTextAlign() != null) {
            validateTextAlign(data.getTextAlign());
        }
    }

    /**
     * 텍스트 요소 속성 업데이트 (Builder 패턴)
     */
    private RoadmapTextElement updateTextElementProperties(
            RoadmapTextElement textElement,
            ActionData data
    ) {
        if (data == null) {
            return textElement;
        }

        String content = textElement.getContent();
        if (data.getTextContent() != null && !data.getTextContent().trim().isEmpty()) {
            content = data.getTextContent();
        }

        Float x = textElement.getX();
        Float y = textElement.getY();
        if (data.getX() != null && data.getY() != null) {
            x = data.getX();
            y = data.getY();
        }

        RoadmapTextElement updated = textElement.withStyle(
                data.getFontSize(),
                data.getTextColor(),
                data.getFontWeight(),
                data.getTextAlign(),
                data.getStyleData()
        );

        return RoadmapTextElement.builder()
                .id(updated.getId())
                .unitId(updated.getUnitId())
                .content(content)
                .x(x)
                .y(y)
                .fontSize(updated.getFontSize())
                .color(updated.getColor())
                .fontWeight(updated.getFontWeight())
                .textAlign(updated.getTextAlign())
                .styleData(updated.getStyleData())
                .locked(updated.getLocked())
                .build();
    }

    /**
     * 색상 검증 (hex code)
     */
    private void validateColor(
            String color
    ) {
        if (!color.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new ActionValidationException(
                    "INVALID_COLOR",
                    "색상은 #RRGGBB 형식의 hex code여야 합니다"
            );
        }
    }

    /**
     * 폰트 굵기 검증 (normal, bold)
     */
    private void validateFontWeight(
            String fontWeight
    ) {
        if (!fontWeight.matches("^(normal|bold)$")) {
            throw new ActionValidationException(
                    "INVALID_FONT_WEIGHT",
                    "폰트 굵기는 normal 또는 bold여야 합니다"
            );
        }
    }

    /**
     * 텍스트 정렬 검증 (left, center, right)
     */
    private void validateTextAlign(
            String textAlign
    ) {
        if (!textAlign.matches("^(left|center|right)$")) {
            throw new ActionValidationException(
                    "INVALID_TEXT_ALIGN",
                    "텍스트 정렬은 left, center, right 중 하나여야 합니다"
            );
        }
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
            RoadmapTextElement updatedTextElement
    ) {
        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("type", "EDIT");

        // target 정보
        Map<String, Object> target = new HashMap<>();
        target.put("type", "TEXT");
        target.put("object", updatedTextElement.getId().toString());
        eventPayload.put("target", target);

        // 수정된 텍스트 요소 상태
        eventPayload.put("state", updatedTextElement.toDto());

        // Event ID 생성
        String eventId = "evt-" + UUID.randomUUID().toString().substring(0, 8);
        Long sequence = System.currentTimeMillis();

        return Event.from(eventId, sequence, eventPayload);
    }
}

