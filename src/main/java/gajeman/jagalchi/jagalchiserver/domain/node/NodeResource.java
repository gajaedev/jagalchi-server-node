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
 * 노드 자료(Resource) 엔티티
 * 노드에 연결된 학습 자료 (링크, 파일, 문서 등)
 *
 * convention.md: 불변 객체 + 빌더 패턴
 */
@Entity
@Table(name = "node_resources", indexes = {
        @Index(name = "idx_node_resources_node_id", columnList = "node_id"),
        @Index(name = "idx_node_resources_unit_id", columnList = "unit_id")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 외부 로드맵 서비스의 unit ID (MSA, FK 없음)
     */
    @Column(name = "unit_id", nullable = false)
    private Long unitId;

    /**
     * 소속 노드 ID
     */
    @Column(name = "node_id", nullable = false)
    private Long nodeId;

    /**
     * 자료 제목/이름
     */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /**
     * 자료 타입 (LINK, PDF, VIDEO, IMAGE, DOCUMENT 등)
     */
    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType;

    /**
     * 자료 URL 또는 경로
     */
    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;

    /**
     * 자료 설명
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 추가 메타데이터 (JSON)
     * 예: 파일크기, MIME 타입, 썸네일 등
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "json")
    private Map<String, Object> metadata;

    /**
     * 표시 순서
     * 기본값: 0 (자동 정렬)
     */
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

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
        dto.put("nodeId", this.nodeId);
        dto.put("title", this.title);
        dto.put("resourceType", this.resourceType);
        dto.put("url", this.url);
        dto.put("description", this.description);
        dto.put("metadata", this.metadata);
        dto.put("displayOrder", this.displayOrder);
        return dto;
    }
}

