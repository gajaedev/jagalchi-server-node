package gajeman.jagalchi.jagalchiserver.domain.progress;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "node_progress", indexes = {
        @Index(name = "idx_node_progress_node_status", columnList = "node_id, status"),
        @Index(name = "idx_node_progress_user_status", columnList = "user_id, status")
})
@IdClass(NodeProgress.NodeProgressId.class)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeProgress {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "node_id", nullable = false)
    private Long nodeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProgressStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodeProgressId implements Serializable {
        private Long userId;
        private Long nodeId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NodeProgressId that = (NodeProgressId) o;
            return java.util.Objects.equals(userId, that.userId) && java.util.Objects.equals(nodeId, that.nodeId);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(userId, nodeId);
        }
    }
}
