package gajeman.jagalchi.jagalchiserver.application.action.handler;

import gajeman.jagalchi.jagalchiserver.domain.action.*;
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
 * EDIT 액션 핸들러
 * logic.md: EDIT 액션은 MOVE, LOCK, COPY, INFO, STATE 등을 포함
 * payload.type으로 세부 동작 구분
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EditActionHandler implements ActionHandler {

    private final RoadmapNodeRepository roadmapNodeRepository;

    @Override
    public Event handle(
            Action action
    ) {
        // 1. payload 검증
        validatePayload(action);

        ActionPayload payload = action.getPayload();
        String payloadType = payload.getType();

        // 2. payload.type별 분기 처리
        switch (payloadType) {
            case "MOVE" -> handleMove(payload);
            case "INFO" -> handleInfo(payload);
            case "SCALE" -> handleScale(payload);
            case "LOCK" -> handleLock(payload);
            case "COPY" -> handleCopy(payload);
            default -> throw new EditorException(ErrorCode.INVALID_INPUT);
        }

        // 3. 수정된 노드 조회 및 Event 생성
        ActionTarget target = payload.getTarget();
        String nodeId = target.getObject();

        RoadmapNode updatedNode = roadmapNodeRepository.findById(Long.parseLong(nodeId))
                .orElseThrow(() -> new EditorException(ErrorCode.NODE_NOT_FOUND));

        return buildEvent(payloadType, updatedNode);
    }

    /**
     * MOVE: 노드 이동
     */
    private void handleMove(
            ActionPayload payload
    ) {
        ActionTarget target = payload.getTarget();
        String nodeId = target.getObject();

        ActionState next = payload.getNext();
        Float x = next.getX();
        Float y = next.getY();

        RoadmapNode node = roadmapNodeRepository.findById(Long.parseLong(nodeId))
                .orElseThrow(() -> new EditorException(ErrorCode.NODE_NOT_FOUND));

        node.move(x, y);
        roadmapNodeRepository.save(node);

        log.info("Node moved: nodeId={}, x={}, y={}", nodeId, x, y);
    }

    /**
     * INFO: 라벨 및 메타데이터 수정
     */
    private void handleInfo(
            ActionPayload payload
    ) {
        ActionTarget target = payload.getTarget();
        String nodeId = target.getObject();

        ActionState next = payload.getNext();

        RoadmapNode node = roadmapNodeRepository.findById(Long.parseLong(nodeId))
                .orElseThrow(() -> new EditorException(ErrorCode.NODE_NOT_FOUND));

        // 라벨 수정
        if (next.getLabel() != null) {
            node.updateLabel(next.getLabel());
        }

        // 메타데이터 수정
        if (next.getMetadata() != null) {
            node.updateData(next.getMetadata());
        }

        roadmapNodeRepository.save(node);

        log.info("Node info updated: nodeId={}", nodeId);
    }


    /**
     * LOCK: 잠금/해제
     */
    private void handleLock(
            ActionPayload payload
    ) {
        ActionTarget target = payload.getTarget();
        String nodeId = target.getObject();

        ActionState next = payload.getNext();
        Boolean locked = next.getLocked();

        RoadmapNode node = roadmapNodeRepository.findById(Long.parseLong(nodeId))
                .orElseThrow(() -> new EditorException(ErrorCode.NODE_NOT_FOUND));

        // locked 값에 따라 잠금/해제
        if (locked != null) {
            if (locked) {
                node.lock();
            } else {
                node.unlock();
            }
        } else {
            // locked가 null이면 토글
            node.toggleLock();
        }

        roadmapNodeRepository.save(node);

        log.info("Node lock changed: nodeId={}, locked={}", nodeId, node.getLocked());
    }

    /**
     * COPY: 노드 복제
     */
    private void handleCopy(
            ActionPayload payload
    ) {
        ActionTarget target = payload.getTarget();
        String sourceNodeId = target.getObject();

        RoadmapNode sourceNode = roadmapNodeRepository.findById(Long.parseLong(sourceNodeId))
                .orElseThrow(() -> new EditorException(ErrorCode.NODE_NOT_FOUND));

        // 새 노드 생성 (약간 오른쪽 아래로 이동)
        RoadmapNode copiedNode = RoadmapNode.builder()
                .unitId(sourceNode.getUnitId())
                .label(sourceNode.getLabel() + " (복사본)")
                .x(sourceNode.getX() + 20)
                .y(sourceNode.getY() + 20)
                .data(sourceNode.getData())
                .build();

        roadmapNodeRepository.save(copiedNode);

        log.info("Node copied: sourceId={}, newId={}", sourceNodeId, copiedNode.getId());
    }

    /**
     * SCALE: 노드 크기 조절
     */
    private void handleScale(ActionPayload payload) {
        ActionTarget target = payload.getTarget();
        String nodeId = target.getObject();
        ActionData data = payload.getData();

        RoadmapNode node = roadmapNodeRepository.findById(Long.parseLong(nodeId))
                .orElseThrow(() -> new EditorException(ErrorCode.NODE_NOT_FOUND));

        Float width = null;
        Float height = null;

        if (data != null && data.getWidth() != null && data.getWidth() > 0) {
            width = data.getWidth();
        }

        if (data != null && data.getHeight() != null && data.getHeight() > 0) {
            height = data.getHeight();
        }

        if (width != null || height != null) {
            RoadmapNode updated = node.withSize(width, height);
            roadmapNodeRepository.save(updated);
            log.info("Node scale changed: nodeId={}, width={}, height={}", nodeId, width, height);
        }
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

        // type 검증
        String type = payload.getType();
        if (type == null) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }

        // target 검증
        ActionTarget target = payload.getTarget();
        if (target == null || target.getType() != TargetType.NODE) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * Event 객체 생성
     */
    private Event buildEvent(
            String payloadType,
            RoadmapNode updatedNode
    ) {
        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("type", payloadType);

        // target 정보
        Map<String, Object> target = new HashMap<>();
        target.put("type", "NODE");
        target.put("object", updatedNode.getId().toString());
        eventPayload.put("target", target);

        // 서버가 확정한 최종 상태
        eventPayload.put("state", updatedNode.toDto());

        // Event ID 생성 (UUID 사용)
        String eventId = "evt-" + UUID.randomUUID().toString().substring(0, 8);

        // Sequence는 시스템 시간 사용
        Long sequence = System.currentTimeMillis();

        return Event.from(eventId, sequence, eventPayload);
    }
}

