package gajeman.jagalchi.jagalchiserver.infrastructure.persistence;

import gajeman.jagalchi.jagalchiserver.domain.node.RoadmapNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 노드 저장소
 */
@Repository
public interface RoadmapNodeRepository extends JpaRepository<RoadmapNode, Long> {
    /**
     * 특정 로드맵(unit)의 모든 노드 조회
     * erd.txt: idx_roadmap_nodes_unit_id 인덱스 사용
     *
     * @param unitId 로드맵 unit ID
     * @return 해당 로드맵의 노드 목록
     */
    List<RoadmapNode> findByUnitId(
            Long unitId
    );

    /**
     * 특정 섹션에 속한 모든 노드 조회
     * SECTION_DESIGN.md: 명시적 관계로 섹션 내 노드 감지
     *
     * @param sectionId 섹션 ID
     * @return 해당 섹션에 속한 노드 목록
     */
    List<RoadmapNode> findBySectionId(
            Long sectionId
    );
}



