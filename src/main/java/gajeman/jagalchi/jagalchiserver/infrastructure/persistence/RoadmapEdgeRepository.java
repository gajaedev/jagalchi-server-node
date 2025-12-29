package gajeman.jagalchi.jagalchiserver.infrastructure.persistence;

import gajeman.jagalchi.jagalchiserver.domain.node.RoadmapEdge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapEdgeRepository extends JpaRepository<RoadmapEdge, Long> {
}

