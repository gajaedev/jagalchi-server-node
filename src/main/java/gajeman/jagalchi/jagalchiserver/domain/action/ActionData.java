package gajeman.jagalchi.jagalchiserver.domain.action;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Action의 data 구조 (CREATE 시 사용)
 *
 * 생성 데이터
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionData {
    /**
     * 노드 라벨
     */
    private String label;

    /**
     * X 좌표
     */
    private Float x;

    /**
     * Y 좌표
     */
    private Float y;

    /**
     * 노드 너비 (SCALE 액션에서 사용)
     */
    private Float width;

    /**
     * 노드 높이 (SCALE 액션에서 사용)
     */
    private Float height;

    /**
     * 추가 메타데이터
     * convention.md: 유연한 구조가 필요한 경우만 Map 허용
     */
    private Map<String, Object> metadata;

    /**
     * 학습 상태 (NOT_STARTED, IN_PROGRESS, COMPLETED)
     * EDIT-STATE 액션에서만 사용
     */
    private String learningState;

    /**
     * 간선 시작 노드 ID
     * CREATE_EDGE 액션에서 사용
     */
    private Long fromNodeId;

    /**
     * 간선 도착 노드 ID
     * CREATE_EDGE 액션에서 사용
     */
    private Long toNodeId;

    /**
     * 간선 스타일 (straight, curved, bezier)
     * CREATE_EDGE / EDIT_EDGE 액션에서 사용
     */
    private String edgeStyle;

    /**
     * 간선 선 색상
     * CREATE_EDGE / EDIT_EDGE 액션에서 사용
     */
    private String strokeColor;

    /**
     * 간선 선 두께
     * CREATE_EDGE / EDIT_EDGE 액션에서 사용
     */
    private Float strokeWidth;

    /**
     * 간선 라벨 텍스트
     * CREATE_EDGE / EDIT_EDGE 액션에서 사용
     */
    private String labelText;

    /**
     * 간선 화살표 타입 (none, single, double)
     * CREATE_EDGE / EDIT_EDGE 액션에서 사용
     */
    private String arrowType;

    /**
     * 간선 방향성 여부
     * CREATE_EDGE / EDIT_EDGE 액션에서 사용
     */
    private Boolean isDirectional;

    /**
     * 간선 애니메이션 타입 (none, pulse, flow)
     * CREATE_EDGE / EDIT_EDGE 액션에서 사용
     */
    private String animationType;

    /**
     * 자료 제목
     * CREATE_RESOURCE / EDIT_RESOURCE 액션에서 사용
     */
    private String resourceTitle;

    /**
     * 자료 타입 (LINK, PDF, VIDEO, IMAGE, DOCUMENT 등)
     * CREATE_RESOURCE / EDIT_RESOURCE 액션에서 사용
     */
    private String resourceType;

    /**
     * 자료 URL
     * CREATE_RESOURCE / EDIT_RESOURCE 액션에서 사용
     */
    private String resourceUrl;

    /**
     * 자료 설명
     * CREATE_RESOURCE / EDIT_RESOURCE 액션에서 사용
     */
    private String resourceDescription;

    /**
     * 자료 메타데이터 (JSON)
     * CREATE_RESOURCE / EDIT_RESOURCE 액션에서 사용
     */
    private Map<String, Object> resourceMetadata;

    /**
     * 자료 표시 순서
     * CREATE_RESOURCE / EDIT_RESOURCE 액션에서 사용
     */
    private Integer displayOrder;

    /**
     * 텍스트 요소 내용
     * CREATE_TEXT / EDIT_TEXT 액션에서 사용
     */
    private String textContent;

    /**
     * 텍스트 폰트 크기
     * CREATE_TEXT / EDIT_TEXT 액션에서 사용
     */
    private Integer fontSize;

    /**
     * 텍스트 색상 (hex code)
     * CREATE_TEXT / EDIT_TEXT 액션에서 사용
     */
    private String textColor;

    /**
     * 텍스트 폰트 굵기 (normal, bold)
     * CREATE_TEXT / EDIT_TEXT 액션에서 사용
     */
    private String fontWeight;

    /**
     * 텍스트 정렬 (left, center, right)
     * CREATE_TEXT / EDIT_TEXT 액션에서 사용
     */
    private String textAlign;

    /**
     * 텍스트 스타일 데이터 (JSON)
     * CREATE_TEXT / EDIT_TEXT 액션에서 사용
     */
    private Map<String, Object> styleData;

    /**
     * 섹션명
     * CREATE_SECTION / EDIT_SECTION 액션에서 사용
     */
    private String sectionName;

    /**
     * 섹션 너비
     * CREATE_SECTION / EDIT_SECTION 액션에서 사용
     */
    private Float sectionWidth;

    /**
     * 섹션 높이
     * CREATE_SECTION / EDIT_SECTION 액션에서 사용
     */
    private Float sectionHeight;

    /**
     * 섹션 색상 (hex code)
     * CREATE_SECTION / EDIT_SECTION 액션에서 사용
     */
    private String sectionColor;

    /**
     * 섹션 설명
     * CREATE_SECTION / EDIT_SECTION 액션에서 사용
     */
    private String sectionDescription;
}
