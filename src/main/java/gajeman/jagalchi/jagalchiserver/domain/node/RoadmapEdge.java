package gajeman.jagalchi.jagalchiserver.domain.node;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "roadmap_edges", indexes = {
        @Index(name = "idx_roadmap_edges_unit_id", columnList = "unit_id"),
        @Index(name = "idx_roadmap_edges_from_node", columnList = "from_node_id"),
        @Index(name = "idx_roadmap_edges_to_node", columnList = "to_node_id"),
        @Index(name = "idx_roadmap_edges_unit_from_to", columnList = "unit_id, from_node_id, to_node_id")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unit_id", nullable = false)
    private Long unitId;

    @Column(name = "from_node_id", nullable = false)
    private Long fromNodeId;

    @Column(name = "to_node_id", nullable = false)
    private Long toNodeId;

    /**
     * 연결선 스타일 (straight, curved, bezier)
     * 기본값: straight
     */
    @Column(name = "style", nullable = false)
    @Builder.Default
    private String style = "straight";

    /**
     * 선 색상 (hex code)
     * 기본값: #000000 (검은색)
     */
    @Column(name = "stroke_color", nullable = false)
    @Builder.Default
    private String strokeColor = "#000000";

    /**
     * 선 두께
     * 기본값: 2.0
     */
    @Column(name = "stroke_width", nullable = false)
    @Builder.Default
    private Float strokeWidth = 2.0f;

    /**
     * 연결선 라벨 텍스트
     * 기본값: "" (빈 문자열)
     */
    @Column(name = "label_text")
    @Builder.Default
    private String labelText = "";

    /**
     * 화살표 타입 (none, single, double)
     * 기본값: single
     */
    @Column(name = "arrow_type", nullable = false)
    @Builder.Default
    private String arrowType = "single";

    /**
     * 방향성 여부
     * true: 방향성 있음 (화살표 표시)
     * false: 양방향
     */
    @Column(name = "is_directional", nullable = false)
    @Builder.Default
    private Boolean isDirectional = true;

    /**
     * 애니메이션 타입 (none, pulse, flow)
     * 기본값: none
     */
    @Column(name = "animation_type", nullable = false)
    @Builder.Default
    private String animationType = "none";

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
     * DTO로 변환 (EVENT 브로드캐스트용)
     */
    public Map<String, Object> toDto() {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", this.id);
        dto.put("fromNodeId", this.fromNodeId);
        dto.put("toNodeId", this.toNodeId);
        dto.put("style", this.style);
        dto.put("strokeColor", this.strokeColor);
        dto.put("strokeWidth", this.strokeWidth);
        dto.put("labelText", this.labelText);
        dto.put("arrowType", this.arrowType);
        dto.put("isDirectional", this.isDirectional);
        dto.put("animationType", this.animationType);
        return dto;
    }
}
