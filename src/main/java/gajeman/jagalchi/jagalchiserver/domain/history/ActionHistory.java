package gajeman.jagalchi.jagalchiserver.domain.history;

import gajeman.jagalchi.jagalchiserver.domain.action.ActionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Action 히스토리 엔티티
 * EVENT 조회 및 UNDO/REDO 기능에 사용
 */
@Entity
@Table(name = "action_history", indexes = {
        @Index(name = "idx_action_history_roadmap_sequence", columnList = "roadmap_id, sequence"),
        @Index(name = "idx_action_history_user", columnList = "user_id, created_at")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 로드맵 ID (외부 서비스의 roadmap ID)
     */
    @Column(name = "roadmap_id", nullable = false)
    private String roadmapId;

    /**
     * 사용자 ID (외부 서비스의 user ID)
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 액션 타입
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    /**
     * Event ID
     */
    @Column(name = "event_id", nullable = false)
    private String eventId;

    /**
     * 시퀀스 번호 (순서 보장)
     */
    @Column(name = "sequence", nullable = false)
    private Long sequence;

    /**
     * Event payload (서버 확정 상태)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_payload", columnDefinition = "json", nullable = false)
    private Map<String, Object> eventPayload;

    /**
     * 이전 상태 (UNDO 시 복원용)
     * UNDO/REDO 기능에서 이전 상태로 돌아갈 때 사용
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "prev_state", columnDefinition = "json")
    private Map<String, Object> prevState;

    /**
     * 생성 시각
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

