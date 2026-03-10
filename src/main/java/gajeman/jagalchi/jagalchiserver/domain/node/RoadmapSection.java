package gajeman.jagalchi.jagalchiserver.domain.node;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 로드맵 섹션 엔티티
 * 캔버스에 표시되는 노드 그룹화 영역
 *
 * convention.md: 불변 객체 + 빌더 패턴
 * SECTION_DESIGN.md: 섹션 설계 기준
 *
 * 핵심 특징:
 * - 섹션은 "가장 깊은 depth"에만 존재
 * - 섹션 이동 시 포함된 노드들도 함께 이동
 * - sectionId 필드로 명시적 관계 관리
 */
@Entity
@Table(name = "roadmap_sections", indexes = {
        @Index(name = "idx_roadmap_sections_unit_id", columnList = "unit_id")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 외부 로드맵 서비스의 unit ID (MSA, FK 없음)
     */
    @Column(name = "unit_id", nullable = false)
    private Long unitId;

    /**
     * 섹션명
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 캔버스 X 좌표 (왼쪽 위 모서리)
     */
    @Column(name = "x_pos", nullable = false)
    private Float x;

    /**
     * 캔버스 Y 좌표 (왼쪽 위 모서리)
     */
    @Column(name = "y_pos", nullable = false)
    private Float y;

    /**
     * 섹션 너비
     */
    @Column(name = "width", nullable = false)
    private Float width;

    /**
     * 섹션 높이
     */
    @Column(name = "height", nullable = false)
    private Float height;

    /**
     * 섹션 색상 (hex code)
     * 기본값: #e8f4f8 (연한 파랑)
     */
    @Column(name = "color", nullable = false)
    @Builder.Default
    private String color = "#e8f4f8";

    /**
     * 섹션 설명
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 잠금 상태 (true: 잠금, false: 해제)
     * 잠금 시 이동·편집 불가
     */
    @Column(name = "locked", nullable = false)
    @Builder.Default
    private Boolean locked = false;

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
     * 섹션 정보 업데이트 (불변 객체 패턴: Builder 사용)
     */
    public RoadmapSection withInfo(String name, String description, String color) {
        return RoadmapSection.builder()
                .id(this.id)
                .unitId(this.unitId)
                .name(name != null ? name : this.name)
                .x(this.x)
                .y(this.y)
                .width(this.width)
                .height(this.height)
                .color(color != null ? color : this.color)
                .description(description != null ? description : this.description)
                .locked(this.locked)
                .build();
    }

    /**
     * 섹션 크기 및 위치 업데이트
     */
    public RoadmapSection withSizeAndPosition(Float x, Float y, Float width, Float height) {
        return RoadmapSection.builder()
                .id(this.id)
                .unitId(this.unitId)
                .name(this.name)
                .x(x != null ? x : this.x)
                .y(y != null ? y : this.y)
                .width(width != null ? width : this.width)
                .height(height != null ? height : this.height)
                .color(this.color)
                .description(this.description)
                .locked(this.locked)
                .build();
    }

    /**
     * DTO로 변환 (EVENT 브로드캐스트용)
     */
    public Map<String, Object> toDto() {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", this.id);
        dto.put("name", this.name);
        dto.put("x", this.x);
        dto.put("y", this.y);
        dto.put("width", this.width);
        dto.put("height", this.height);
        dto.put("color", this.color);
        dto.put("description", this.description);
        dto.put("locked", this.locked);
        return dto;
    }
}

