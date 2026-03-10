package gajeman.jagalchi.jagalchiserver.domain.action;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Action의 state 구조 (prev/next)
 *
 * 상태 정보
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionState {
    /**
     * X 좌표
     */
    private Float x;

    /**
     * Y 좌표
     */
    private Float y;

    /**
     * 노드 라벨
     */
    private String label;

    /**
     * 잠금 상태
     */
    private Boolean locked;

    /**
     * 추가 메타데이터 (유연한 구조를 위해 Map 사용)
     * convention.md: 유연한 구조가 필요한 경우만 Map 허용
     * 색상, 설명 등 자유로운 속성
     */
    private Map<String, Object> metadata;
}

