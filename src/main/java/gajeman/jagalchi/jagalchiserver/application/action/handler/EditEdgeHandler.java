package gajeman.jagalchi.jagalchiserver.application.action.handler;

import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionData;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionPayload;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionTarget;
import gajeman.jagalchi.jagalchiserver.domain.event.Event;
import gajeman.jagalchi.jagalchiserver.domain.node.RoadmapEdge;
import gajeman.jagalchi.jagalchiserver.domain.payload.TargetType;
import gajeman.jagalchi.jagalchiserver.global.exception.ActionValidationException;
import gajeman.jagalchi.jagalchiserver.global.exception.EditorException;
import gajeman.jagalchi.jagalchiserver.global.exception.ErrorCode;
import gajeman.jagalchi.jagalchiserver.infrastructure.persistence.RoadmapEdgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 간선(연결선) 수정 핸들러
 * logic.md: EDIT 액션 (EDGE 대상)
 * convention.md 섹션 12: 불변 객체 패턴
 *
 * 간선의 스타일, 색상, 라벨, 화살표, 애니메이션 수정
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EditEdgeHandler implements ActionHandler {

    private final RoadmapEdgeRepository roadmapEdgeRepository;

    @Override
    public Event handle(
            Action action
    ) {
        // 1. payload 검증
        validatePayload(action);

        ActionPayload payload = action.getPayload();
        ActionTarget target = payload.getTarget();
        ActionData data = payload.getData();

        // 2. 간선 ID 추출 및 조회
        String edgeIdStr = target.getObject();
        if (edgeIdStr == null) {
            throw new ActionValidationException(
                    "INVALID_EDGE_ID",
                    "간선 ID가 없습니다"
            );
        }

        Long edgeId = Long.parseLong(edgeIdStr);
        RoadmapEdge edge = roadmapEdgeRepository.findById(edgeId)
                .orElseThrow(() -> new EditorException(ErrorCode.INVALID_INPUT));

        // 3. 검증만 수행 (Builder 패턴 준비)
        if (data != null) {
            if (data.getEdgeStyle() != null) {
                validateStyle(data.getEdgeStyle());
            }
            if (data.getStrokeColor() != null) {
                validateColor(data.getStrokeColor());
            }
            if (data.getStrokeWidth() != null && data.getStrokeWidth() <= 0) {
                throw new ActionValidationException(
                        "INVALID_STROKE_WIDTH",
                        "선 두께는 0보다 커야 합니다"
                );
            }
            if (data.getArrowType() != null) {
                validateArrowType(data.getArrowType());
            }
            if (data.getAnimationType() != null) {
                validateAnimationType(data.getAnimationType());
            }
        }

        // 4. Builder 패턴으로 새 간선 객체 생성
        RoadmapEdge.RoadmapEdgeBuilder builder = RoadmapEdge.builder()
                .id(edge.getId())
                .unitId(edge.getUnitId())
                .fromNodeId(edge.getFromNodeId())
                .toNodeId(edge.getToNodeId())
                .style(data != null && data.getEdgeStyle() != null ? data.getEdgeStyle() : edge.getStyle())
                .strokeColor(data != null && data.getStrokeColor() != null ? data.getStrokeColor() : edge.getStrokeColor())
                .strokeWidth(data != null && data.getStrokeWidth() != null ? data.getStrokeWidth() : edge.getStrokeWidth())
                .labelText(data != null && data.getLabelText() != null ? data.getLabelText() : edge.getLabelText())
                .arrowType(data != null && data.getArrowType() != null ? data.getArrowType() : edge.getArrowType())
                .isDirectional(data != null && data.getIsDirectional() != null ? data.getIsDirectional() : edge.getIsDirectional())
                .animationType(data != null && data.getAnimationType() != null ? data.getAnimationType() : edge.getAnimationType());

        RoadmapEdge updatedEdge = builder.build();

        // 5. 저장
        RoadmapEdge savedEdge = roadmapEdgeRepository.save(updatedEdge);

        log.info("Edge updated: edgeId={}, style={}, strokeColor={}, labelText={}",
                edgeId, savedEdge.getStyle(), savedEdge.getStrokeColor(), savedEdge.getLabelText());

        // 6. Event 생성
        return buildEvent(savedEdge);
    }

    /**
     * 간선 속성 업데이트
     * convention.md 섹션 12: 불변 객체 패턴
     */
    private void updateEdgeProperties(
            RoadmapEdge edge,
            ActionData data
    ) {
        if (data == null) {
            return;
        }

        // 스타일 업데이트
        if (data.getEdgeStyle() != null) {
            validateStyle(data.getEdgeStyle());
            // Builder 패턴으로 새 객체 생성 (불변성 보장)
            // 주의: 실제 저장은 updateEdge 반환값으로 처리
        }

        // 색상 업데이트
        if (data.getStrokeColor() != null) {
            validateColor(data.getStrokeColor());
        }

        // 두께 업데이트
        if (data.getStrokeWidth() != null) {
            if (data.getStrokeWidth() <= 0) {
                throw new ActionValidationException(
                        "INVALID_STROKE_WIDTH",
                        "선 두께는 0보다 커야 합니다"
                );
            }
        }

        // 라벨 업데이트
        if (data.getLabelText() != null) {
            // 라벨은 null 허용
        }

        // 화살표 타입 업데이트
        if (data.getArrowType() != null) {
            validateArrowType(data.getArrowType());
        }

        // 방향성 업데이트
        if (data.getIsDirectional() != null) {
            // 방향성 업데이트
        }

        // 애니메이션 업데이트
        if (data.getAnimationType() != null) {
            validateAnimationType(data.getAnimationType());
        }
    }

    /**
     * 스타일 검증 (straight, curved, bezier)
     */
    private void validateStyle(
            String style
    ) {
        if (!style.matches("^(straight|curved|bezier)$")) {
            throw new ActionValidationException(
                    "INVALID_STYLE",
                    "스타일은 straight, curved, bezier 중 하나여야 합니다"
            );
        }
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
     * 화살표 타입 검증 (none, single, double)
     */
    private void validateArrowType(
            String arrowType
    ) {
        if (!arrowType.matches("^(none|single|double)$")) {
            throw new ActionValidationException(
                    "INVALID_ARROW_TYPE",
                    "화살표 타입은 none, single, double 중 하나여야 합니다"
            );
        }
    }

    /**
     * 애니메이션 타입 검증 (none, pulse, flow)
     */
    private void validateAnimationType(
            String animationType
    ) {
        if (!animationType.matches("^(none|pulse|flow)$")) {
            throw new ActionValidationException(
                    "INVALID_ANIMATION_TYPE",
                    "애니메이션 타입은 none, pulse, flow 중 하나여야 합니다"
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
            RoadmapEdge updatedEdge
    ) {
        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("type", "EDIT");

        // target 정보
        Map<String, Object> target = new HashMap<>();
        target.put("type", "EDGE");
        target.put("object", updatedEdge.getId().toString());
        eventPayload.put("target", target);

        // 수정된 간선 상태
        eventPayload.put("state", updatedEdge.toDto());

        // Event ID 생성
        String eventId = "evt-" + UUID.randomUUID().toString().substring(0, 8);
        Long sequence = System.currentTimeMillis();

        return Event.from(eventId, sequence, eventPayload);
    }
}

