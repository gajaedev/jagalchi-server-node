package gajeman.jagalchi.jagalchiserver.domain.node;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 로드맵 노드 엔티티
 * 캔버스에 표시되는 학습 노드를 나타냄
 */
@Entity
@Table(name = "roadmap_nodes", indexes = {
        @Index(name = "idx_roadmap_nodes_unit_id", columnList = "unit_id")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 외부 로드맵 서비스의 unit ID (MSA, FK 없음)
     */
    @Column(name = "unit_id", nullable = false)
    private Long unitId;

    /**
     * 노드 제목/라벨
     */
    @Column(name = "label", nullable = false)
    private String label;

    /**
     * 캔버스 X 좌표
     */
    @Column(name = "x_pos", nullable = false)
    private Float x;

    /**
     * 캔버스 Y 좌표
     */
    @Column(name = "y_pos", nullable = false)
    private Float y;

    /**
     * 노드 너비 (기본값: null - 클라이언트에서 동적 계산)
     */
    @Column(name = "width")
    private Float width;

    /**
     * 노드 높이 (기본값: null - 클라이언트에서 동적 계산)
     */
    @Column(name = "height")
    private Float height;

    /**
     * 추가 메타데이터 (JSON)
     * 예: 링크, 난이도, 태그, 리소스 URL, 썸네일 등
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "json")
    private Map<String, Object> data;

    /**
     * 잠금 상태 (true: 잠금, false: 해제)
     * 잠금 시 이동·편집 불가
     */
    @Column(name = "locked", nullable = false)
    @Builder.Default
    private Boolean locked = false;

    /**
     * 학습 상태 (NOT_STARTED, IN_PROGRESS, COMPLETED)
     * 기본값: NOT_STARTED
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "learning_state", nullable = false)
    @Builder.Default
    private LearningState learningState = LearningState.NOT_STARTED;

    /**
     * 소속 섹션 ID
     * null이면 어떤 섹션에도 속하지 않음
     * convention.md: 명시적 관계로 관리
     */
    @Column(name = "section_id")
    private Long sectionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 노드 이동
     */
    public void move(
            Float x,
            Float y
    ) {
        this.x = x;
        this.y = y;
    }

    /**
     * 라벨 수정
     */
    public void updateLabel(
            String label
    ) {
        this.label = label;
    }

    /**
     * 메타데이터 수정
     */
    public void updateData(
            Map<String, Object> data
    ) {
        this.data = data;
    }

    /**
     * 잠금 설정
     */
    public void lock() {
        this.locked = true;
    }

    /**
     * 잠금 해제
     */
    public void unlock() {
        this.locked = false;
    }

    /**
     * 잠금 토글
     */
    public void toggleLock() {
        this.locked = !this.locked;
    }

    /**
     * 학습 상태를 다음 단계로 진행
     * NOT_STARTED → IN_PROGRESS → COMPLETED → NOT_STARTED (순환)
     */
    public void advanceLearningState() {
        this.learningState = switch (this.learningState) {
            case NOT_STARTED -> LearningState.IN_PROGRESS;
            case IN_PROGRESS -> LearningState.COMPLETED;
            case COMPLETED -> LearningState.NOT_STARTED;
        };
    }


    /**
     * 크기 조절 (불변 객체 패턴: Builder 사용)
     * convention.md: 불변 객체 + 빌더 패턴
     */
    public RoadmapNode withSize(Float width, Float height) {
        return RoadmapNode.builder()
                .id(this.id)
                .unitId(this.unitId)
                .label(this.label)
                .x(this.x)
                .y(this.y)
                .width(width != null ? width : this.width)
                .height(height != null ? height : this.height)
                .data(this.data)
                .locked(this.locked)
                .learningState(this.learningState)
                .sectionId(this.sectionId)
                .build();
    }

    /**
     * DTO로 변환 (EVENT 브로드캐스트용)
     */
    public Map<String, Object> toDto() {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", this.id);
        dto.put("label", this.label);
        dto.put("x", this.x);
        dto.put("y", this.y);
        dto.put("width", this.width);
        dto.put("height", this.height);
        dto.put("data", this.data);
        dto.put("locked", this.locked);
        dto.put("learningState", this.learningState.name());
        return dto;
    }
}
