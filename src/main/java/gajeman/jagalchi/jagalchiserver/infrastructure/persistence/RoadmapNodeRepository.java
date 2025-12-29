package gajeman.jagalchi.jagalchiserver.infrastructure.persistence;

import gajeman.jagalchi.jagalchiserver.domain.node.RoadmapNode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapNodeRepository extends JpaRepository<RoadmapNode, Long> {
}

