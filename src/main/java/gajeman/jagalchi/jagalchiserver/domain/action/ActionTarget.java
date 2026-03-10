package gajeman.jagalchi.jagalchiserver.domain.action;

import gajeman.jagalchi.jagalchiserver.domain.payload.TargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Action의 target 구조
 * 액션 대상 정보
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionTarget {
    /**
     * 대상 타입 (NODE, GROUP, SECTION, EDGE)
     * convention.md: 타입 정보가 고정되면 Enum으로 관리
     */
    private TargetType type;

    /**
     * 대상 객체 ID
     */
    private String object;

    /**
     * 임시 ID (CREATE 시 클라이언트가 생성한 tempId)
     */
    private String tempId;

    /**
     * 소속 노드 ID (자료 생성 시 필요)
     */
    private Long nodeId;
}

