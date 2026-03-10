package gajeman.jagalchi.jagalchiserver.infrastructure.persistence;

import gajeman.jagalchi.jagalchiserver.domain.node.RoadmapSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 로드맵 섹션 저장소
 */
@Repository
public interface RoadmapSectionRepository extends JpaRepository<RoadmapSection, Long> {

    /**
     * 특정 로드맵의 모든 섹션 조회
     */
    List<RoadmapSection> findByUnitId(
            Long unitId
    );
}

