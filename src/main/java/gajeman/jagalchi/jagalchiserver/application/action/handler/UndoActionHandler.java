package gajeman.jagalchi.jagalchiserver.application.action.handler;

import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.event.Event;
import gajeman.jagalchi.jagalchiserver.domain.node.RoadmapNode;
import gajeman.jagalchi.jagalchiserver.domain.undo.CompletionItem;
import gajeman.jagalchi.jagalchiserver.domain.undo.UndoRedoManager;
import gajeman.jagalchi.jagalchiserver.global.exception.EditorException;
import gajeman.jagalchi.jagalchiserver.global.exception.ErrorCode;
import gajeman.jagalchi.jagalchiserver.infrastructure.persistence.RoadmapNodeRepository;
import gajeman.jagalchi.jagalchiserver.infrastructure.undo.UndoRedoManagerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * UNDO 액션 핸들러
 * convention.md 섹션 12: Done Stack 기반 UNDO 구현
 *
 * 동작:
 * 1. UndoRedoManager 조회
 * 2. canUndo() 검증
 * 3. manager.undo() 호출
 * 4. previousState로 노드 복원
 * 5. manager 저장
 * 6. Event 브로드캐스트
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UndoActionHandler implements ActionHandler {

    private final RoadmapNodeRepository roadmapNodeRepository;
    private final UndoRedoManagerRepository undoRedoManagerRepository;

    @Override
    public Event handle(
            Action action
    ) {
        // 1. 로드맵 ID와 사용자 ID 추출
        String roadmapId = action.getRoadmap();
        Long userId = extractUserId(action);

        // 2. Redis에서 UNDO/REDO 관리자 조회
        UndoRedoManager manager = undoRedoManagerRepository.getManager(roadmapId, userId);

        // 3. UNDO 가능 여부 확인
        if (!manager.canUndo()) {
            log.warn("Cannot undo: roadmapId={}, userId={}", roadmapId, userId);
            throw new EditorException(ErrorCode.INVALID_INPUT);  // "실행 취소할 작업이 없습니다"
        }

        // 4. UNDO 실행
        CompletionItem undoneItem = manager.undo();

        if (undoneItem == null) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }

        // 5. previousState로 노드 복원
        Long nodeId = undoneItem.entityId();
        RoadmapNode node = roadmapNodeRepository.findById(nodeId)
                .orElseThrow(() -> new EditorException(ErrorCode.NODE_NOT_FOUND));

        Map<String, Object> previousState = undoneItem.previousState();
        if (previousState != null) {
            node.updateLabel((String) previousState.get("label"));
            node.move(
                    ((Number) previousState.get("x")).floatValue(),
                    ((Number) previousState.get("y")).floatValue()
            );
            if (previousState.containsKey("data")) {
                node.updateData((Map<String, Object>) previousState.get("data"));
            }
        }

        RoadmapNode restoredNode = roadmapNodeRepository.save(node);

        // 6. 관리자 상태 저장
        undoRedoManagerRepository.saveManager(manager);

        log.info("Action undone: roadmapId={}, userId={}, nodeId={}, canUndo={}, canRedo={}",
                roadmapId, userId, nodeId, manager.canUndo(), manager.canRedo());

        // 7. Event 생성
        return buildEvent(restoredNode);
    }

    /**
     * Action에서 userId 추출
     */
    private Long extractUserId(
            Action action
    ) {
        // TODO: ActionContext에서 추출하도록 개선 필요
        return 1L;  // 임시값
    }

    /**
     * Event 객체 생성
     */
    private Event buildEvent(
            RoadmapNode restoredNode
    ) {
        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("type", "UNDO");

        // target 정보
        Map<String, Object> target = new HashMap<>();
        target.put("type", "NODE");
        target.put("object", restoredNode.getId().toString());
        eventPayload.put("target", target);

        // 복원된 상태
        eventPayload.put("state", restoredNode.toDto());

        // Event ID 생성
        String eventId = "evt-" + UUID.randomUUID().toString().substring(0, 8);
        Long sequence = System.currentTimeMillis();

        return Event.from(eventId, sequence, eventPayload);
    }
}

