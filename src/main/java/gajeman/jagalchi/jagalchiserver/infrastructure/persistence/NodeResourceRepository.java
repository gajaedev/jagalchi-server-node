package gajeman.jagalchi.jagalchiserver.infrastructure.persistence;

import gajeman.jagalchi.jagalchiserver.domain.node.NodeResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 노드 자료(Resource) 저장소
 */
@Repository
public interface NodeResourceRepository extends JpaRepository<NodeResource, Long> {

    /**
     * 특정 노드의 모든 자료 조회
     */
    List<NodeResource> findByNodeId(
            Long nodeId
    );

    /**
     * 특정 로드맵의 모든 자료 조회
     */
    List<NodeResource> findByUnitId(
            Long unitId
    );
}

