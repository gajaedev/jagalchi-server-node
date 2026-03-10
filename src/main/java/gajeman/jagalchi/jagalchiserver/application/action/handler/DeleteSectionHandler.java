package gajeman.jagalchi.jagalchiserver.application.action.handler;

import gajeman.jagalchi.jagalchiserver.domain.action.Action;
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
 * 섹션 삭제 핸들러
 * logic.md: DELETE 액션 (SECTION 대상)
 * SECTION_DESIGN.md: 섹션 삭제 시 노드는 유지
 *
 * 섹션만 삭제되고 노드들은 sectionId를 null로 설정하여 유지
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeleteSectionHandler implements ActionHandler {

    private final RoadmapSectionRepository roadmapSectionRepository;
    private final RoadmapNodeRepository roadmapNodeRepository;

    @Override
    public Event handle(
            Action action
    ) {
        // 1. payload 검증
        validatePayload(action);

        ActionPayload payload = action.getPayload();
        ActionTarget target = payload.getTarget();

        // 2. 섹션 ID 추출 및 조회
        String sectionIdStr = target.getObject();
        if (sectionIdStr == null) {
            throw new ActionValidationException(
                    "INVALID_SECTION_ID",
                    "섹션 ID가 없습니다"
            );
        }

        Long sectionId = Long.parseLong(sectionIdStr);
        RoadmapSection section = roadmapSectionRepository.findById(sectionId)
                .orElseThrow(() -> new EditorException(ErrorCode.INVALID_INPUT));

        // 3. 섹션 내 노드들의 sectionId를 null로 설정 (Builder 패턴)
        List<RoadmapNode> nodesInSection = roadmapNodeRepository.findBySectionId(sectionId);
        List<RoadmapNode> updatedNodes = nodesInSection.stream()
                .map(node -> RoadmapNode.builder()
                        .id(node.getId())
                        .unitId(node.getUnitId())
                        .label(node.getLabel())
                        .x(node.getX())
                        .y(node.getY())
                        .width(node.getWidth())
                        .height(node.getHeight())
                        .data(node.getData())
                        .locked(node.getLocked())
                        .learningState(node.getLearningState())
                        .sectionId(null)
                        .build())
                .toList();

        if (!updatedNodes.isEmpty()) {
            roadmapNodeRepository.saveAll(updatedNodes);
            log.info("Nodes in section unlinked from section: sectionId={}, nodeCount={}",
                    sectionId, nodesInSection.size());
        }

        // 4. 섹션 삭제
        roadmapSectionRepository.deleteById(sectionId);

        log.info("Section deleted: sectionId={}, name={}", sectionId, section.getName());

        // 5. Event 생성
        return buildEvent(section);
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
            RoadmapSection deletedSection
    ) {
        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("type", "DELETE");

        // target 정보
        Map<String, Object> target = new HashMap<>();
        target.put("type", "SECTION");
        target.put("object", deletedSection.getId().toString());
        eventPayload.put("target", target);

        // 삭제된 섹션 정보
        Map<String, Object> state = new HashMap<>();
        state.put("id", deletedSection.getId());
        state.put("name", deletedSection.getName());
        eventPayload.put("state", state);

        // Event ID 생성
        String eventId = "evt-" + UUID.randomUUID().toString().substring(0, 8);
        Long sequence = System.currentTimeMillis();

        return Event.from(eventId, sequence, eventPayload);
    }
}

