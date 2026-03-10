package gajeman.jagalchi.jagalchiserver.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Snapshot DTO (logic.md 기준)
 * 초기 연결 시 전체 상태를 제공하는 응답
 *
 * 예시:
 * {
 *   "type": "SNAPSHOT",
 *   "version": 42,
 *   "roadmapId": "roadmap-1",
 *   "nodes": [ ... ],
 *   "edges": [ ... ],
 *   "sections": [ ... ]
 * }
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Snapshot {
    /**
     * 응답 타입 (항상 SNAPSHOT)
     */
    private EventType type;

    /**
     * 상태 버전 (순서 보장용)
     */
    private Long version;

    /**
     * 로드맵 ID
     */
    private String roadmapId;

    /**
     * 노드 목록
     */
    private List<Map<String, Object>> nodes;

    /**
     * 엣지 목록
     */
    private List<Map<String, Object>> edges;

    /**
     * 섹션 목록
     */
    private List<Map<String, Object>> sections;

    /**
     * 고아 노드 ID 목록 (간선이 없는 노드들)
     * 클라이언트에서 고립된 노드를 시각적으로 처리할 수 있도록 제공
     */
    private List<Long> orphanNodeIds;

    /**
     * Snapshot 생성 factory method
     */
    public static Snapshot from(
            Long version,
            String roadmapId,
            List<Map<String, Object>> nodes,
            List<Map<String, Object>> edges,
            List<Map<String, Object>> sections,
            List<Long> orphanNodeIds
    ) {
        return Snapshot.builder()
                .type(EventType.SNAPSHOT)
                .version(version)
                .roadmapId(roadmapId)
                .nodes(nodes)
                .edges(edges)
                .sections(sections)
                .orphanNodeIds(orphanNodeIds)
                .build();
    }
}
