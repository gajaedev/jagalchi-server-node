package gajeman.jagalchi.jagalchiserver.infrastructure.persistence;

import gajeman.jagalchi.jagalchiserver.domain.history.ActionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Action 히스토리 Repository
 * EVENT 조회 및 UNDO/REDO 기능에 사용
 */
public interface ActionHistoryRepository extends JpaRepository<ActionHistory, Long> {
    /**
     * 특정 로드맵의 특정 sequence 이후의 히스토리 조회
     * EVENT 조회 기능에 사용 (재연결 시 놓친 이벤트 가져오기)
     *
     * @param roadmapId 로드맵 ID
     * @param sequence 시작 sequence (이 값보다 큰 것만 조회)
     * @return 히스토리 목록 (sequence 오름차순)
     */
    List<ActionHistory> findByRoadmapIdAndSequenceGreaterThanOrderBySequenceAsc(
            String roadmapId,
            Long sequence
    );

    /**
     * 특정 로드맵의 최근 히스토리 조회 (UNDO용)
     *
     * @param roadmapId 로드맵 ID
     * @param userId 사용자 ID
     * @return 히스토리 목록 (sequence 내림차순)
     */
    List<ActionHistory> findByRoadmapIdAndUserIdOrderBySequenceDesc(
            String roadmapId,
            Long userId
    );
}

