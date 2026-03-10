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
 * 텍스트 요소 엔티티
 * 캔버스에 표시되는 독립적인 텍스트 요소
 *
 * convention.md: 불변 객체 + 빌더 패턴
 */
@Entity
@Table(name = "roadmap_text_elements", indexes = {
        @Index(name = "idx_roadmap_text_unit_id", columnList = "unit_id")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapTextElement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 외부 로드맵 서비스의 unit ID (MSA, FK 없음)
     */
    @Column(name = "unit_id", nullable = false)
    private Long unitId;

    /**
     * 텍스트 내용
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

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
     * 텍스트 크기
     * 기본값: 16
     */
    @Column(name = "font_size", nullable = false)
    @Builder.Default
    private Integer fontSize = 16;

    /**
     * 텍스트 색상 (hex code)
     * 기본값: #000000 (검은색)
     */
    @Column(name = "color", nullable = false)
    @Builder.Default
    private String color = "#000000";

    /**
     * 폰트 굵기 (normal, bold)
     * 기본값: normal
     */
    @Column(name = "font_weight", nullable = false)
    @Builder.Default
    private String fontWeight = "normal";

    /**
     * 텍스트 정렬 (left, center, right)
     * 기본값: left
     */
    @Column(name = "text_align", nullable = false)
    @Builder.Default
    private String textAlign = "left";

    /**
     * 추가 스타일 정보 (JSON)
     * 예: 투명도, 배경색, 테두리 등
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "style_data", columnDefinition = "json")
    private Map<String, Object> styleData;

    /**
     * 잠금 상태 (true: 잠금, false: 해제)
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
     * 텍스트 내용 수정
     */
    public void updateContent(
            String content
    ) {
        this.content = content;
    }

    /**
     * 위치 이동
     */
    public void move(
            Float x,
            Float y
    ) {
        this.x = x;
        this.y = y;
    }

    /**
     * 스타일 업데이트 (불변 객체 패턴: Builder 사용)
     * convention.md: 불변 객체 + 빌더 패턴
     */
    public RoadmapTextElement withStyle(
            Integer fontSize,
            String color,
            String fontWeight,
            String textAlign,
            Map<String, Object> styleData
    ) {
        return RoadmapTextElement.builder()
                .id(this.id)
                .unitId(this.unitId)
                .content(this.content)
                .x(this.x)
                .y(this.y)
                .fontSize(fontSize != null ? fontSize : this.fontSize)
                .color(color != null ? color : this.color)
                .fontWeight(fontWeight != null ? fontWeight : this.fontWeight)
                .textAlign(textAlign != null ? textAlign : this.textAlign)
                .styleData(styleData != null ? styleData : this.styleData)
                .locked(this.locked)
                .build();
    }

    /**
     * DTO로 변환 (EVENT 브로드캐스트용)
     */
    public Map<String, Object> toDto() {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", this.id);
        dto.put("content", this.content);
        dto.put("x", this.x);
        dto.put("y", this.y);
        dto.put("fontSize", this.fontSize);
        dto.put("color", this.color);
        dto.put("fontWeight", this.fontWeight);
        dto.put("textAlign", this.textAlign);
        dto.put("styleData", this.styleData);
        dto.put("locked", this.locked);
        return dto;
    }
}

