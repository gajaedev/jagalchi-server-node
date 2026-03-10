package gajeman.jagalchi.jagalchiserver.application.action.handler;

import gajeman.jagalchi.jagalchiserver.domain.action.Action;
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
 * 텍스트 요소 삭제 핸들러
 * logic.md: DELETE 액션 (TEXT 대상)
 *
 * 캔버스의 텍스트 요소 삭제
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeleteTextElementHandler implements ActionHandler {

    private final RoadmapTextElementRepository roadmapTextElementRepository;

    @Override
    public Event handle(
            Action action
    ) {
        // 1. payload 검증
        validatePayload(action);

        ActionPayload payload = action.getPayload();
        ActionTarget target = payload.getTarget();

        // 2. 텍스트 요소 ID 추출 및 조회
        String textIdStr = target.getObject();
        if (textIdStr == null) {
            throw new ActionValidationException(
                    "INVALID_TEXT_ID",
                    "텍스트 요소 ID가 없습니다"
            );
        }

        Long textId = Long.parseLong(textIdStr);
        RoadmapTextElement textElement = roadmapTextElementRepository.findById(textId)
                .orElseThrow(() -> new EditorException(ErrorCode.INVALID_INPUT));

        // 3. 텍스트 요소 정보 저장 (Event에 포함하기 위해)
        String content = textElement.getContent();

        // 4. 텍스트 요소 삭제
        roadmapTextElementRepository.deleteById(textId);

        log.info("Text element deleted: textId={}, content={}", textId, content);

        // 5. Event 생성
        return buildEvent(textId, content);
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
            Long textId,
            String content
    ) {
        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("type", "DELETE");

        // target 정보
        Map<String, Object> target = new HashMap<>();
        target.put("type", "TEXT");
        target.put("object", textId.toString());
        eventPayload.put("target", target);

        // 삭제된 텍스트 요소 정보
        Map<String, Object> state = new HashMap<>();
        state.put("id", textId);
        state.put("content", content);
        eventPayload.put("state", state);

        // Event ID 생성
        String eventId = "evt-" + UUID.randomUUID().toString().substring(0, 8);
        Long sequence = System.currentTimeMillis();

        return Event.from(eventId, sequence, eventPayload);
    }
}

