package gajeman.jagalchi.jagalchiserver.application.action.handler;

import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionData;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionPayload;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionTarget;
import gajeman.jagalchi.jagalchiserver.domain.event.Event;
import gajeman.jagalchi.jagalchiserver.domain.node.RoadmapNode;
import gajeman.jagalchi.jagalchiserver.domain.node.LearningState;
import gajeman.jagalchi.jagalchiserver.global.exception.EditorException;
import gajeman.jagalchi.jagalchiserver.global.exception.ErrorCode;
import gajeman.jagalchi.jagalchiserver.infrastructure.persistence.RoadmapNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 노드 학습 상태 변경 핸들러
 * logic.md: EDIT-STATE 액션 처리
 *
 * 노드의 학습 상태를 변경 (NOT_STARTED → IN_PROGRESS → COMPLETED)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EditStateChangeHandler implements ActionHandler {

    private final RoadmapNodeRepository roadmapNodeRepository;

    @Override
    public Event handle(
            Action action
    ) {
        // 1. payload 검증
        validatePayload(action);

        ActionPayload payload = action.getPayload();
        ActionTarget target = payload.getTarget();
        ActionData data = payload.getData();

        // 2. 노드 ID 추출
        String nodeId = target.getObject();
        RoadmapNode node = roadmapNodeRepository.findById(Long.parseLong(nodeId))
                .orElseThrow(() -> new EditorException(ErrorCode.NODE_NOT_FOUND));

        // 3. 이전 상태 저장 (UNDO용)
        LearningState previousState = node.getLearningState();

        // 4. 학습 상태 변경 (Builder 패턴)
        LearningState newState = previousState;
        if (data.getLearningState() != null) {
            newState = LearningState.fromString(data.getLearningState());
        } else {
            // 상태 전환: NOT_STARTED -> IN_PROGRESS -> COMPLETED
            newState = switch (previousState) {
                case NOT_STARTED -> LearningState.IN_PROGRESS;
                case IN_PROGRESS -> LearningState.COMPLETED;
                case COMPLETED -> LearningState.COMPLETED;  // 마지막 상태
            };
        }

        RoadmapNode updated = RoadmapNode.builder()
                .id(node.getId())
                .unitId(node.getUnitId())
                .label(node.getLabel())
                .x(node.getX())
                .y(node.getY())
                .width(node.getWidth())
                .height(node.getHeight())
                .data(node.getData())
                .locked(node.getLocked())
                .learningState(newState)
                .sectionId(node.getSectionId())
                .build();

        RoadmapNode updatedNode = roadmapNodeRepository.save(updated);

        log.info("Node learning state changed: nodeId={}, before={}, after={}",
                nodeId, previousState, node.getLearningState());

        // 5. Event 생성
        return buildEvent(updatedNode);
    }

    /**
     * Payload 검증
     */
    private void validatePayload(
            Action action
    ) {
        ActionPayload payload = action.getPayload();
        if (payload == null) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }

        ActionTarget target = payload.getTarget();
        if (target == null || target.getObject() == null) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * Event 생성
     */
    private Event buildEvent(
            RoadmapNode updatedNode
    ) {
        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("type", "EDIT");

        // target 정보
        Map<String, Object> target = new HashMap<>();
        target.put("type", "NODE");
        target.put("object", updatedNode.getId().toString());
        eventPayload.put("target", target);

        // 수정된 상태
        eventPayload.put("state", updatedNode.toDto());

        // Event ID 생성
        String eventId = "evt-" + UUID.randomUUID().toString().substring(0, 8);
        Long sequence = System.currentTimeMillis();

        return Event.from(eventId, sequence, eventPayload);
    }
}

