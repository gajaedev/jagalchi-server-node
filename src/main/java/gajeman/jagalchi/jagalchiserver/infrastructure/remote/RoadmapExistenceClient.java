package gajeman.jagalchi.jagalchiserver.infrastructure.remote;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * 외부 로드맵 서비스 존재 여부를 확인하는 간단한 클라이언트
 * - external.roadmap.base-url 프로퍼티가 설정되어 있으면 해당 서비스에 GET /api/roadmap/{id} 요청하여 존재 여부 확인
 * - 설정이 없는 경우(개발환경)에는 true를 반환하여 기존 동작을 보존
 */
@Component
@Slf4j
public class RoadmapExistenceClient {

    @Value("${external.roadmap.base-url:}")
    private String baseUrl;

    private RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public boolean exists(Long roadmapId) {
        if (baseUrl == null || baseUrl.isBlank()) {
            // 외부 서비스 설정이 없으면 존재 여부 확인을 건너뜀(기본 허용)
            return true;
        }

        String url = baseUrl.replaceAll("/$", "") + "/api/roadmap/" + roadmapId;
        try {
            ResponseEntity<String> res = restTemplate().getForEntity(url, String.class);
            return res.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("Roadmap not found: {}", roadmapId);
            return false;
        } catch (Exception e) {
            log.warn("Roadmap existence check failed for {}: {}", roadmapId, e.getMessage());
            // 외부 체크 실패 시 보수적으로 false로 처리하여 안전성 확보
            return false;
        }
    }
}
