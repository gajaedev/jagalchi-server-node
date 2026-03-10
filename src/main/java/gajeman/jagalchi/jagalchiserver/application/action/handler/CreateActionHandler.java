package gajeman.jagalchi.jagalchiserver.application.action.handler;

import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionData;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionPayload;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionTarget;
import gajeman.jagalchi.jagalchiserver.domain.event.Event;
import gajeman.jagalchi.jagalchiserver.domain.node.RoadmapNode;
import gajeman.jagalchi.jagalchiserver.domain.payload.TargetType;
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
 * CREATE 액션 핸들러
 * 새로운 노드를 생성하여 저장하고 EVENT를 생성
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CreateActionHandler implements ActionHandler {

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

        // 2. 데이터 추출
        String tempId = target.getTempId() != null ? target.getTempId() : target.getObject();
        String label = data.getLabel() != null ? data.getLabel() : "새 노드";
        Float x = data.getX();
        Float y = data.getY();
        Map<String, Object> metadata = data.getMetadata();

        // 3. unitId 추출 (roadmap ID를 Long으로 변환)
        Long unitId = Long.parseLong(action.getRoadmap());

        // 4. RoadmapNode 엔티티 생성 및 저장
        RoadmapNode roadmapNode = RoadmapNode.builder()
                .unitId(unitId)
                .label(label)
                .x(x)
                .y(y)
                .data(metadata)
                .build();

        RoadmapNode savedNode = roadmapNodeRepository.save(roadmapNode);

        log.info("Node created: id={}, unitId={}, label={}",
                savedNode.getId(), savedNode.getUnitId(), savedNode.getLabel());

        // 5. Event 생성 (서버 확정 상태)
        return buildEvent(savedNode, tempId);
    }

    /**
     * payload 검증
     */
    private void validatePayload(
            Action action
    ) {
        ActionPayload payload = action.getPayload();
        if (payload == null) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }

        // target 검증
        ActionTarget target = payload.getTarget();
        if (target == null || target.getType() != TargetType.NODE) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }

        // data 검증
        ActionData data = payload.getData();
        if (data == null) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }

        if (data.getX() == null || data.getY() == null) {
            throw new EditorException(ErrorCode.NODE_POSITION_REQUIRED);
        }
    }

    /**
     * Event 객체 생성
     */
    private Event buildEvent(
            RoadmapNode savedNode,
            String tempId
    ) {
        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("type", "CREATE");

        // target 정보
        Map<String, Object> target = new HashMap<>();
        target.put("type", "NODE");
        target.put("object", savedNode.getId().toString());
        target.put("tempId", tempId);  // 클라이언트가 매칭에 사용
        eventPayload.put("target", target);

        // 서버가 확정한 최종 상태
        eventPayload.put("state", savedNode.toDto());

        // Event ID 생성 (UUID 사용)
        String eventId = "evt-" + UUID.randomUUID().toString().substring(0, 8);

        // Sequence는 추후 히스토리 관리 시 사용 (현재는 시스템 시간)
        Long sequence = System.currentTimeMillis();

        return Event.from(eventId, sequence, eventPayload);
    }
}

