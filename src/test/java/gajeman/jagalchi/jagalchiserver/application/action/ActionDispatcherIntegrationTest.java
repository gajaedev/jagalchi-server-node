package gajeman.jagalchi.jagalchiserver.application.action;

import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionAck;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionType;
import gajeman.jagalchi.jagalchiserver.domain.auth.ActionContext;
import gajeman.jagalchi.jagalchiserver.domain.auth.UserRole;
import gajeman.jagalchi.jagalchiserver.domain.node.RoadmapNode;
import gajeman.jagalchi.jagalchiserver.global.exception.CustomException;
import gajeman.jagalchi.jagalchiserver.global.exception.ErrorCode;
import gajeman.jagalchi.jagalchiserver.infrastructure.persistence.RoadmapNodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ActionDispatcher 통합 테스트")
class ActionDispatcherIntegrationTest {

    @Autowired
    private ActionDispatcher actionDispatcher;

    @Autowired
    private RoadmapNodeRepository roadmapNodeRepository;

    @Autowired
    private ActionPipelineValidator pipelineValidator;

    private ActionContext userContext;
    private ActionContext adminContext;
    private ActionContext guestContext;

    @BeforeEach
    void setUp() {
        userContext = ActionContext.of(1L, UserRole.USER);
        adminContext = ActionContext.of(2L, UserRole.ADMIN);
        guestContext = ActionContext.guest();
    }

    @Nested
    @DisplayName("NODE_CREATE 액션")
    class NodeCreateAction {

        @Test
        @DisplayName("성공: USER 권한으로 노드 생성")
        void createNode_withUserRole_success() {
            // given
            String actionId = UUID.randomUUID().toString();
            Map<String, Object> payload = new HashMap<>();
            payload.put("tempId", "temp-123");
            payload.put("label", "테스트 노드");
            payload.put("x", 100.0f);
            payload.put("y", 200.0f);

            Action action = Action.builder()
                    .actionId(actionId)
                    .roadmapId(1L)
                    .actorId(1L)
                    .actionType(ActionType.NODE_CREATE)
                    .payload(payload)
                    .clientTimestamp(LocalDateTime.now())
                    .build();

            // when
            ActionAck ack = actionDispatcher.dispatch(action, userContext);

            // then
            assertThat(ack).isNotNull();
            assertThat(ack.actionId()).isEqualTo(actionId);
            assertThat(ack.actionType()).isEqualTo(ActionType.NODE_CREATE);
            assertThat(ack.serverState()).containsKey("nodeId");
            assertThat(ack.serverState().get("tempId")).isEqualTo("temp-123");

            // DB 저장 확인
            Long nodeId = ((Number) ack.serverState().get("nodeId")).longValue();
            Optional<RoadmapNode> savedNode = roadmapNodeRepository.findById(nodeId);
            assertThat(savedNode).isPresent();
            assertThat(savedNode.get().getLabel()).isEqualTo("테스트 노드");
        }

        @Test
        @DisplayName("성공: ADMIN 권한으로 노드 생성")
        void createNode_withAdminRole_success() {
            // given
            String actionId = UUID.randomUUID().toString();
            Map<String, Object> payload = new HashMap<>();
            payload.put("tempId", "temp-admin");
            payload.put("label", "관리자 노드");
            payload.put("x", 150.0f);
            payload.put("y", 250.0f);

            Action action = Action.builder()
                    .actionId(actionId)
                    .roadmapId(1L)
                    .actorId(2L)
                    .actionType(ActionType.NODE_CREATE)
                    .payload(payload)
                    .clientTimestamp(LocalDateTime.now())
                    .build();

            // when
            ActionAck ack = actionDispatcher.dispatch(action, adminContext);

            // then
            assertThat(ack).isNotNull();
            assertThat(ack.serverState().get("label")).isEqualTo("관리자 노드");
        }

        @Test
        @DisplayName("실패: GUEST 권한으로 노드 생성 시도")
        void createNode_withGuestRole_fail() {
            // given
            String actionId = UUID.randomUUID().toString();
            Map<String, Object> payload = new HashMap<>();
            payload.put("tempId", "temp-guest");
            payload.put("x", 100.0f);
            payload.put("y", 200.0f);

            Action action = Action.builder()
                    .actionId(actionId)
                    .roadmapId(1L)
                    .actorId(null)
                    .actionType(ActionType.NODE_CREATE)
                    .payload(payload)
                    .clientTimestamp(LocalDateTime.now())
                    .build();

            // when & then
            assertThatThrownBy(() -> actionDispatcher.dispatch(action, guestContext))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.UNAUTHORIZED);
        }

        @Test
        @DisplayName("실패: 좌표 누락 시 예외 발생")
        void createNode_withoutPosition_fail() {
            // given
            String actionId = UUID.randomUUID().toString();
            Map<String, Object> payload = new HashMap<>();
            payload.put("tempId", "temp-no-position");
            payload.put("label", "좌표 없는 노드");
            // x, y 좌표 누락

            Action action = Action.builder()
                    .actionId(actionId)
                    .roadmapId(1L)
                    .actorId(1L)
                    .actionType(ActionType.NODE_CREATE)
                    .payload(payload)
                    .clientTimestamp(LocalDateTime.now())
                    .build();

            // when & then
            assertThatThrownBy(() -> actionDispatcher.dispatch(action, userContext))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.NODE_POSITION_REQUIRED);
        }
    }

    @Nested
    @DisplayName("actionId 중복 체크")
    class ActionIdDuplication {

        @Test
        @DisplayName("실패: 동일한 actionId로 중복 요청")
        void duplicateActionId_fail() {
            // given
            String duplicateActionId = UUID.randomUUID().toString();
            Map<String, Object> payload = new HashMap<>();
            payload.put("tempId", "temp-dup");
            payload.put("label", "중복 테스트 노드");
            payload.put("x", 100.0f);
            payload.put("y", 200.0f);

            Action firstAction = Action.builder()
                    .actionId(duplicateActionId)
                    .roadmapId(1L)
                    .actorId(1L)
                    .actionType(ActionType.NODE_CREATE)
                    .payload(payload)
                    .clientTimestamp(LocalDateTime.now())
                    .build();

            Action duplicateAction = Action.builder()
                    .actionId(duplicateActionId)
                    .roadmapId(1L)
                    .actorId(1L)
                    .actionType(ActionType.NODE_CREATE)
                    .payload(payload)
                    .clientTimestamp(LocalDateTime.now())
                    .build();

            // when - 첫 번째 요청 성공
            ActionAck ack = actionDispatcher.dispatch(firstAction, userContext);
            assertThat(ack).isNotNull();

            // then - 두 번째 요청은 실패
            assertThatThrownBy(() -> actionDispatcher.dispatch(duplicateAction, userContext))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.DUPLICATE_ACTION);

            // cleanup - 테스트 후 actionId 제거 (다른 테스트에 영향 방지)
            pipelineValidator.removeFromProcessed(duplicateActionId);
        }
    }

    @Nested
    @DisplayName("지원하지 않는 ActionType")
    class UnsupportedActionType {

        @Test
        @DisplayName("실패: 미구현 ActionType 요청")
        void unsupportedActionType_fail() {
            // given
            String actionId = UUID.randomUUID().toString();
            Map<String, Object> payload = new HashMap<>();
            payload.put("nodeId", 1L);
            payload.put("x", 300.0f);
            payload.put("y", 400.0f);

            Action action = Action.builder()
                    .actionId(actionId)
                    .roadmapId(1L)
                    .actorId(1L)
                    .actionType(ActionType.NODE_MOVE) // 아직 미구현
                    .payload(payload)
                    .clientTimestamp(LocalDateTime.now())
                    .build();

            // when & then
            assertThatThrownBy(() -> actionDispatcher.dispatch(action, userContext))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_ACTION_TYPE);
        }
    }
}
