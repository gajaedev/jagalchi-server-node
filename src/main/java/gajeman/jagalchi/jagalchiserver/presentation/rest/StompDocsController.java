package gajeman.jagalchi.jagalchiserver.presentation.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StompDocsController {

    @Operation(
            summary = "STOMP/WebSocket 연결 정보 조회",
            description = "Swagger에서 확인할 수 있는 STOMP 연결 안내용 엔드포인트입니다. " +
                    "실제 STOMP 연결 endpoint, broker prefix, publish/subscribe destination, 필요한 헤더를 반환합니다.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "STOMP 연결 정보 반환",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StompDocsResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              \"endpoint\": \"/ws/roadmap\",
                                              \"sockJsEnabled\": true,
                                              \"applicationDestinationPrefix\": \"/app\",
                                              \"brokerPrefixes\": [\"/topic\", \"/queue\", \"/user\"],
                                              \"connectHeaders\": [\"X-User-ID\", \"X-User-Role\", \"X-Roadmap-ID\", \"X-Permissions\"],
                                              \"publishDestinations\": [
                                                \"/app/roadmap/{roadmapId}/action\",
                                                \"/app/roadmap/{roadmapId}/cursor\",
                                                \"/app/roadmap/{roadmapId}/cursor/hide\"
                                              ],
                                              \"subscribeDestinations\": [
                                                \"/user/queue/ack\",
                                                \"/user/queue/nack\",
                                                \"/topic/roadmap/{roadmapId}/state\",
                                                \"/topic/roadmap/{roadmapId}/cursors\",
                                                \"/topic/roadmap/{roadmapId}/cursors/hide\"
                                              ]
                                            }
                                            """
                            )
                    )
            )
    )
    @GetMapping("/stomp/info")
    public StompDocsResponse stompInfo() {
        return new StompDocsResponse(
                "/ws/roadmap",
                true,
                "/app",
                List.of("/topic", "/queue", "/user"),
                List.of("X-User-ID", "X-User-Role", "X-Roadmap-ID", "X-Permissions"),
                List.of(
                        "/app/roadmap/{roadmapId}/action",
                        "/app/roadmap/{roadmapId}/cursor",
                        "/app/roadmap/{roadmapId}/cursor/hide"
                ),
                List.of(
                        "/user/queue/ack",
                        "/user/queue/nack",
                        "/topic/roadmap/{roadmapId}/state",
                        "/topic/roadmap/{roadmapId}/cursors",
                        "/topic/roadmap/{roadmapId}/cursors/hide"
                )
        );
    }

    public record StompDocsResponse(
            String endpoint,
            boolean sockJsEnabled,
            String applicationDestinationPrefix,
            List<String> brokerPrefixes,
            List<String> connectHeaders,
            List<String> publishDestinations,
            List<String> subscribeDestinations
    ) {
    }
}
