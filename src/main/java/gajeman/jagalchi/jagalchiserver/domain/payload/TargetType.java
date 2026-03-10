package gajeman.jagalchi.jagalchiserver.domain.payload;

/**
 * Target 타입 (logic.md 기준)
 * 액션의 대상 객체 타입
 */
public enum TargetType {
    NODE,        // 노드
    GROUP,       // 그룹
    SECTION,     // 섹션
    EDGE,        // 연결선
    TEXT,        // 텍스트 요소
    RESOURCE     // 자료
}

