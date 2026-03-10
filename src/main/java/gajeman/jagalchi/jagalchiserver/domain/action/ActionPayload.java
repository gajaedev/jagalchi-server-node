package gajeman.jagalchi.jagalchiserver.domain.action;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Action의 payload 구조
 *
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionPayload {
    /**
     * payload 타입 (INFO, MOVE, SCALE, LOCK, COPY)
     */
    private String type;

    /**
     * 대상 정보
     */
    private ActionTarget target;

    /**
     * 이전 상태 (클라이언트 참조용)
     */
    private ActionState prev;

    /**
     * 다음 상태 (서버가 적용할 상태)
     */
    private ActionState next;

    /**
     * 추가 데이터 (CREATE 시 사용)
     */
    private ActionData data;
}

