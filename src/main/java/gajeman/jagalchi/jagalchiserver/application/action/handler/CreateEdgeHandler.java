package gajeman.jagalchi.jagalchiserver.application.action.handler;

import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionData;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionPayload;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionTarget;
import gajeman.jagalchi.jagalchiserver.domain.event.Event;
import gajeman.jagalchi.jagalchiserver.domain.node.RoadmapEdge;
import gajeman.jagalchi.jagalchiserver.domain.node.RoadmapNode;
import gajeman.jagalchi.jagalchiserver.domain.payload.TargetType;
import gajeman.jagalchi.jagalchiserver.global.exception.ActionValidationException;
import gajeman.jagalchi.jagalchiserver.global.exception.EditorException;
import gajeman.jagalchi.jagalchiserver.global.exception.ErrorCode;
import gajeman.jagalchi.jagalchiserver.infrastructure.persistence.RoadmapEdgeRepository;
import gajeman.jagalchi.jagalchiserver.infrastructure.persistence.RoadmapNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 간선(연결선) 생성 핸들러
 * logic.md: CREATE 액션 (EDGE 대상)
 *
 * 두 노드 간 간선 생성
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CreateEdgeHandler implements ActionHandler {

    private final RoadmapEdgeRepository roadmapEdgeRepository;
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
        Long fromNodeId = data.getFromNodeId();
        Long toNodeId = data.getToNodeId();

        if (fromNodeId == null || toNodeId == null) {
            throw new ActionValidationException(
                    "INVALID_EDGE_NODES",
                    "fromNodeId와 toNodeId는 필수입니다"
            );
        }

        // 3. 노드 존재 여부 검증
        RoadmapNode fromNode = roadmapNodeRepository.findById(fromNodeId)
                .orElseThrow(() -> new EditorException(ErrorCode.NODE_NOT_FOUND));

        RoadmapNode toNode = roadmapNodeRepository.findById(toNodeId)
                .orElseThrow(() -> new EditorException(ErrorCode.NODE_NOT_FOUND));

        // 4. 비즈니스 규칙 검증
        // 자기 자신으로의 간선 방지
        if (fromNodeId.equals(toNodeId)) {
            throw new ActionValidationException(
                    "SELF_REFERENCE_EDGE",
                    "노드는 자기 자신으로 연결될 수 없습니다"
            );
        }

        // 로드맵 일치 검증
        Long unitId = Long.parseLong(action.getRoadmap());
        if (!fromNode.getUnitId().equals(unitId) || !toNode.getUnitId().equals(unitId)) {
            throw new ActionValidationException(
                    "INVALID_ROADMAP_ID",
                    "노드들이 다른 로드맵에 속합니다"
            );
        }

        // 중복 간선 검증
        Optional<RoadmapEdge> existingEdge = roadmapEdgeRepository.findExistingEdge(
                unitId,
                fromNodeId,
                toNodeId
        );

        if (existingEdge.isPresent()) {
            throw new ActionValidationException(
                    "DUPLICATE_EDGE",
                    "이미 같은 두 노드 사이에 간선이 존재합니다"
            );
        }

        // 5. 간선 생성
        RoadmapEdge edge = RoadmapEdge.builder()
                .unitId(unitId)
                .fromNodeId(fromNodeId)
                .toNodeId(toNodeId)
                .style(data.getEdgeStyle() != null ? data.getEdgeStyle() : "straight")
                .strokeColor(data.getStrokeColor() != null ? data.getStrokeColor() : "#000000")
                .strokeWidth(data.getStrokeWidth() != null ? data.getStrokeWidth() : 2.0f)
                .labelText(data.getLabelText() != null ? data.getLabelText() : "")
                .arrowType(data.getArrowType() != null ? data.getArrowType() : "single")
                .isDirectional(data.getIsDirectional() != null ? data.getIsDirectional() : true)
                .animationType(data.getAnimationType() != null ? data.getAnimationType() : "none")
                .build();

        RoadmapEdge createdEdge = roadmapEdgeRepository.save(edge);

        log.info("Edge created: id={}, fromNodeId={}, toNodeId={}, unitId={}",
                createdEdge.getId(), fromNodeId, toNodeId, unitId);

        // 6. Event 생성
        return buildEvent(createdEdge);
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
        if (target == null || target.getType() != TargetType.EDGE) {
            throw new ActionValidationException(
                    "INVALID_TARGET",
                    "대상이 간선(EDGE)이어야 합니다"
            );
        }
    }

    /**
     * Event 생성
     */
    private Event buildEvent(
            RoadmapEdge createdEdge
    ) {
        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("type", "CREATE");

        // target 정보
        Map<String, Object> target = new HashMap<>();
        target.put("type", "EDGE");
        target.put("object", createdEdge.getId().toString());
        eventPayload.put("target", target);

        // 생성된 간선 상태
        eventPayload.put("state", createdEdge.toDto());

        // Event ID 생성
        String eventId = "evt-" + UUID.randomUUID().toString().substring(0, 8);
        Long sequence = System.currentTimeMillis();

        return Event.from(eventId, sequence, eventPayload);
    }
}

