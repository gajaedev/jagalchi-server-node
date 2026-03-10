package gajeman.jagalchi.jagalchiserver.application.action.usecase;

import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.action.ActionAck;

/**
 * 로드맵 액션 처리 유즈케이스
 */
public interface ExecuteRoadmapActionUseCase {
    /**
     * 액션 실행
     * @param roadmapId 로드맵 ID
     * @param userId 사용자 ID
     * @param userRole 사용자 역할
     * @param action 액션 객체
     * @return 처리 결과 Ack
     */
    ActionAck execute(Long roadmapId, String userId, String userRole, Action action);
}

