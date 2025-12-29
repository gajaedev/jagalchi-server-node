package gajeman.jagalchi.jagalchiserver.application.action;

import gajeman.jagalchi.jagalchiserver.application.auth.PermissionValidator;
import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionType;
import gajeman.jagalchi.jagalchiserver.domain.auth.ActionContext;
import gajeman.jagalchi.jagalchiserver.domain.auth.UserRole;
import gajeman.jagalchi.jagalchiserver.global.exception.CustomException;
import gajeman.jagalchi.jagalchiserver.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ActionPipelineValidator 단위 테스트")
class ActionPipelineValidatorTest {

    @InjectMocks
    private ActionPipelineValidator pipelineValidator;

    @Mock
    private PermissionValidator permissionValidator;

    private Action createTestAction(String actionId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("x", 100.0f);
        payload.put("y", 200.0f);

        return Action.builder()
                .actionId(actionId)
                .roadmapId(1L)
                .actorId(1L)
                .actionType(ActionType.NODE_CREATE)
                .payload(payload)
                .clientTimestamp(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("인증 검증")
    class AuthenticationValidation {

        @Test
        @DisplayName("성공: 인증된 사용자")
        void authenticatedUser_success() {
            // given
            ActionContext context = ActionContext.of(1L, UserRole.USER);
            Action action = createTestAction(UUID.randomUUID().toString());
            given(permissionValidator.canEditRoadmap(any(), anyLong())).willReturn(true);

            // when & then
            assertThatCode(() -> pipelineValidator.validate(action, context))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("실패: 미인증 사용자 (GUEST)")
        void guestUser_fail() {
            // given
            ActionContext context = ActionContext.guest();
            Action action = createTestAction(UUID.randomUUID().toString());

            // when & then
            assertThatThrownBy(() -> pipelineValidator.validate(action, context))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.UNAUTHORIZED);
        }

        @Test
        @DisplayName("실패: userId가 null인 경우")
        void nullUserId_fail() {
            // given
            ActionContext context = ActionContext.of(null, UserRole.USER);
            Action action = createTestAction(UUID.randomUUID().toString());

            // when & then
            assertThatThrownBy(() -> pipelineValidator.validate(action, context))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("권한 검증")
    class PermissionValidation {

        @Test
        @DisplayName("성공: 편집 권한 있음")
        void hasEditPermission_success() {
            // given
            ActionContext context = ActionContext.of(1L, UserRole.USER);
            Action action = createTestAction(UUID.randomUUID().toString());
            given(permissionValidator.canEditRoadmap(context, 1L)).willReturn(true);

            // when & then
            assertThatCode(() -> pipelineValidator.validate(action, context))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("실패: 편집 권한 없음")
        void noEditPermission_fail() {
            // given
            ActionContext context = ActionContext.of(1L, UserRole.USER);
            Action action = createTestAction(UUID.randomUUID().toString());
            given(permissionValidator.canEditRoadmap(context, 1L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> pipelineValidator.validate(action, context))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("actionId 중복 검증")
    class ActionIdDuplicationValidation {

        @BeforeEach
        void setUp() {
            given(permissionValidator.canEditRoadmap(any(), anyLong())).willReturn(true);
        }

        @Test
        @DisplayName("성공: 유니크한 actionId")
        void uniqueActionId_success() {
            // given
            ActionContext context = ActionContext.of(1L, UserRole.USER);
            Action action = createTestAction(UUID.randomUUID().toString());

            // when & then
            assertThatCode(() -> pipelineValidator.validate(action, context))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("실패: 중복된 actionId")
        void duplicateActionId_fail() {
            // given
            ActionContext context = ActionContext.of(1L, UserRole.USER);
            String duplicateId = UUID.randomUUID().toString();
            Action firstAction = createTestAction(duplicateId);
            Action duplicateAction = createTestAction(duplicateId);

            // 첫 번째 요청은 성공
            pipelineValidator.validate(firstAction, context);

            // when & then - 두 번째 요청은 실패
            assertThatThrownBy(() -> pipelineValidator.validate(duplicateAction, context))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.DUPLICATE_ACTION);
        }

        @Test
        @DisplayName("실패: null actionId")
        void nullActionId_fail() {
            // given
            ActionContext context = ActionContext.of(1L, UserRole.USER);
            Action action = createTestAction(null);

            // when & then
            assertThatThrownBy(() -> pipelineValidator.validate(action, context))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("실패: 빈 문자열 actionId")
        void emptyActionId_fail() {
            // given
            ActionContext context = ActionContext.of(1L, UserRole.USER);
            Action action = createTestAction("   ");

            // when & then
            assertThatThrownBy(() -> pipelineValidator.validate(action, context))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_INPUT);
        }
    }

    @Nested
    @DisplayName("처리 상태 관리")
    class ProcessedStateManagement {

        @Test
        @DisplayName("removeFromProcessed 후 재처리 가능")
        void removeAndReprocess_success() {
            // given
            ActionContext context = ActionContext.of(1L, UserRole.USER);
            given(permissionValidator.canEditRoadmap(any(), anyLong())).willReturn(true);

            String actionId = UUID.randomUUID().toString();
            Action action = createTestAction(actionId);

            // 첫 번째 처리
            pipelineValidator.validate(action, context);

            // 처리 목록에서 제거 (실패 시 재시도 허용)
            pipelineValidator.removeFromProcessed(actionId);

            // when & then - 재처리 가능
            assertThatCode(() -> pipelineValidator.validate(action, context))
                    .doesNotThrowAnyException();
        }
    }
}
