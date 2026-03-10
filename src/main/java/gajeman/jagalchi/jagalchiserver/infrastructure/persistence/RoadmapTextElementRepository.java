package gajeman.jagalchi.jagalchiserver.infrastructure.persistence;

import gajeman.jagalchi.jagalchiserver.domain.node.RoadmapTextElement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 로드맵 텍스트 요소 저장소
 */
@Repository
public interface RoadmapTextElementRepository extends JpaRepository<RoadmapTextElement, Long> {

    /**
     * 특정 로드맵의 모든 텍스트 요소 조회
     */
    List<RoadmapTextElement> findByUnitId(
            Long unitId
    );
}

