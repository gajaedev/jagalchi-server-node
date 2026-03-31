package gajeman.jagalchi.jagalchiserver.application.action.filter;

import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionType;
import gajeman.jagalchi.jagalchiserver.domain.payload.TargetType;

/**
 * 활동 기록 필터
 * Streak 업데이트 대상 활동을 판단
 */
public class ActivityTypeFilter {

    /**
     * 활동 기록이 필요한 액션인지 판단
     * 
     * 높은 우선순위 (반드시 포함):
     * - 노드 생성 (CREATE + NODE)
     * - 섹션 생성 (CREATE + SECTION)
     * - 리소스 추가 (CREATE + RESOURCE)
     * - 노드 상태 변경 (EDIT + NODE, payload type이 STATE인 경우)
     * 
     * 제외:
     * - UNDO/REDO (실제 활동 아님)
     * - 노드 이동/크기 조절 (EDIT + MOVE/SCALE)
     * - 잠금/복사 (EDIT + LOCK/COPY)
     * 
     * @param action 액션 객체
     * @return 활동 기록이 필요하면 true
     */
    public static boolean shouldRecordActivity(Action action) {
        if (action == null || action.getAction() == null) {
            return false;
        }

        ActionType actionType = action.getAction();
        
        // UNDO/REDO는 제외
        if (actionType == ActionType.UNDO || actionType == ActionType.REDO) {
            return false;
        }

        // CREATE 액션 처리
        if (actionType == ActionType.CREATE) {
            return shouldRecordCreateAction(action);
        }

        // EDIT 액션 처리
        if (actionType == ActionType.EDIT) {
            return shouldRecordEditAction(action);
        }

        // DELETE는 선택적으로 포함하지 않음 (필요시 추가 가능)
        return false;
    }

    /**
     * CREATE 액션이 기록 대상인지 판단
     */
    private static boolean shouldRecordCreateAction(Action action) {
        if (action.getPayload() == null || action.getPayload().getTarget() == null) {
            return false;
        }

        TargetType targetType = action.getPayload().getTarget().getType();
        
        // 노드, 섹션, 리소스 생성은 모두 의미있는 활동
        return targetType == TargetType.NODE 
            || targetType == TargetType.SECTION 
            || targetType == TargetType.RESOURCE
            || targetType == TargetType.TEXT;  // 텍스트 노트 작성도 포함
    }

    /**
     * EDIT 액션이 기록 대상인지 판단
     */
    private static boolean shouldRecordEditAction(Action action) {
        if (action.getPayload() == null) {
            return false;
        }

        String payloadType = action.getPayload().getType();
        
        // STATE 변경 (노드 완료 등)은 의미있는 활동
        if ("STATE".equalsIgnoreCase(payloadType)) {
            return true;
        }

        // INFO 수정 (노드 정보 변경)도 포함
        if ("INFO".equalsIgnoreCase(payloadType)) {
            TargetType targetType = action.getPayload().getTarget() != null 
                ? action.getPayload().getTarget().getType() 
                : null;
            return targetType == TargetType.NODE;
        }

        // MOVE, SCALE, LOCK, COPY는 제외
        return false;
    }
}
