package gajeman.jagalchi.jagalchiserver.application.action.handler;

import gajeman.jagalchi.jagalchiserver.domain.action.Action;
import gajeman.jagalchi.jagalchiserver.domain.event.Event;

/**
 * Action 처리 핸들러 인터페이스
 * 각 ActionType별로 구현체를 만들어 처리
 */
public interface ActionHandler {
    /**
     * Action 처리
     *
     * @param action 처리할 액션
     * @return Event 서버가 확정한 상태를 담은 이벤트
     */
    Event handle(
            Action action
    );
}

