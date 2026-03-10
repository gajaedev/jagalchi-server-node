package gajeman.jagalchi.jagalchiserver.application.action.handler;

import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionData;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionPayload;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionTarget;
import gajeman.jagalchi.jagalchiserver.domain.event.Event;
import gajeman.jagalchi.jagalchiserver.domain.node.RoadmapNode;
import gajeman.jagalchi.jagalchiserver.domain.node.RoadmapSection;
import gajeman.jagalchi.jagalchiserver.domain.payload.TargetType;
import gajeman.jagalchi.jagalchiserver.global.exception.ActionValidationException;
import gajeman.jagalchi.jagalchiserver.global.exception.EditorException;
import gajeman.jagalchi.jagalchiserver.global.exception.ErrorCode;
import gajeman.jagalchi.jagalchiserver.infrastructure.persistence.RoadmapNodeRepository;
import gajeman.jagalchi.jagalchiserver.infrastructure.persistence.RoadmapSectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 섹션 수정 핸들러
 * logic.md: EDIT 액션 (SECTION 대상)
 * SECTION_DESIGN.md: 섹션 수정 및 노드 함께 이동
 *
 * ⚠️ 핵심 기능: 섹션 이동 시 포함된 노드들도 함께 이동
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EditSectionHandler implements ActionHandler {

    private final RoadmapSectionRepository roadmapSectionRepository;
    private final RoadmapNodeRepository roadmapNodeRepository;

    @Override
    public Event handle(Action action) {
        validatePayload(action);

        ActionPayload payload = action.getPayload();
        ActionTarget target = payload.getTarget();
        ActionData data = payload.getData();

        String sectionIdStr = target.getObject();
        if (sectionIdStr == null) {
            throw new ActionValidationException("INVALID_SECTION_ID", "섹션 ID가 없습니다");
        }

        Long sectionId = Long.parseLong(sectionIdStr);
        RoadmapSection section = roadmapSectionRepository.findById(sectionId)
                .orElseThrow(() -> new EditorException(ErrorCode.INVALID_INPUT));

        Float oldX = section.getX();
        Float oldY = section.getY();

        // Builder 패턴으로 업데이트
        RoadmapSection updated = updateSectionProperties(section, data);
        RoadmapSection savedSection = roadmapSectionRepository.save(updated);

        // 위치가 변경되었다면 섹션 내 노드들도 함께 이동
        Float newX = savedSection.getX();
        Float newY = savedSection.getY();

        if (!oldX.equals(newX) || !oldY.equals(newY)) {
            float dx = newX - oldX;
            float dy = newY - oldY;

            if (dx != 0 || dy != 0) {
                moveNodesInSection(sectionId, dx, dy);
                log.info("Nodes in section moved: sectionId={}, dx={}, dy={}", sectionId, dx, dy);
            }
        }

        log.info("Section updated: sectionId={}, name={}, position=({}, {})",
                sectionId, savedSection.getName(), savedSection.getX(), savedSection.getY());

        return buildEvent(savedSection);
    }

    /**
     * 섹션 속성 업데이트
     */
    private RoadmapSection updateSectionProperties(
            RoadmapSection section,
            ActionData data
    ) {
        if (data == null) {
            return section;
        }

        // 검증
        if (data.getSectionName() != null && data.getSectionName().length() > 100) {
            throw new ActionValidationException("SECTION_NAME_TOO_LONG", "섹션명은 100자 이하여야 합니다");
        }

        if (data.getSectionColor() != null) {
            validateColor(data.getSectionColor());
        }

        if (data.getSectionWidth() != null && data.getSectionWidth() <= 0) {
            throw new ActionValidationException("INVALID_SECTION_SIZE", "섹션 크기는 0보다 커야 합니다");
        }

        if (data.getSectionHeight() != null && data.getSectionHeight() <= 0) {
            throw new ActionValidationException("INVALID_SECTION_SIZE", "섹션 크기는 0보다 커야 합니다");
        }

        if (data.getX() != null && data.getX() < 0) {
            throw new ActionValidationException("INVALID_SECTION_POSITION", "섹션 위치는 0 이상이어야 합니다");
        }

        if (data.getY() != null && data.getY() < 0) {
            throw new ActionValidationException("INVALID_SECTION_POSITION", "섹션 위치는 0 이상이어야 합니다");
        }

        // Builder 패턴으로 업데이트
        RoadmapSection updated = section.withInfo(
                data.getSectionName(),
                data.getSectionDescription(),
                data.getSectionColor()
        );

        // 크기와 위치 업데이트
        if (data.getSectionWidth() != null || data.getSectionHeight() != null ||
            data.getX() != null || data.getY() != null) {
            updated = updated.withSizeAndPosition(
                    data.getX(),
                    data.getY(),
                    data.getSectionWidth(),
                    data.getSectionHeight()
            );
        }

        return updated;
    }

    /**
     * 섹션 내 노드들 이동 (Builder 패턴)
     */
    private void moveNodesInSection(Long sectionId, Float dx, Float dy) {
        List<RoadmapNode> nodesInSection = roadmapNodeRepository.findBySectionId(sectionId);

        // Builder 패턴으로 각 노드 이동
        List<RoadmapNode> updatedNodes = nodesInSection.stream()
                .map(node -> RoadmapNode.builder()
                        .id(node.getId())
                        .unitId(node.getUnitId())
                        .label(node.getLabel())
                        .x(node.getX() + dx)
                        .y(node.getY() + dy)
                        .width(node.getWidth())
                        .height(node.getHeight())
                        .data(node.getData())
                        .locked(node.getLocked())
                        .learningState(node.getLearningState())
                        .sectionId(node.getSectionId())
                        .build())
                .toList();

        if (!updatedNodes.isEmpty()) {
            roadmapNodeRepository.saveAll(updatedNodes);
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
            RoadmapSection updatedSection
    ) {
        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("type", "EDIT");

        // target 정보
        Map<String, Object> target = new HashMap<>();
        target.put("type", "SECTION");
        target.put("object", updatedSection.getId().toString());
        eventPayload.put("target", target);

        // 수정된 섹션 상태
        eventPayload.put("state", updatedSection.toDto());

        // Event ID 생성
        String eventId = "evt-" + UUID.randomUUID().toString().substring(0, 8);
        Long sequence = System.currentTimeMillis();

        return Event.from(eventId, sequence, eventPayload);
    }
}

