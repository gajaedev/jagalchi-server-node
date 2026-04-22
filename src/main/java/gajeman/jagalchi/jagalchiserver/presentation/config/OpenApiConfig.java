package gajeman.jagalchi.jagalchiserver.presentation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Jagalchi Node API")
                        .version("v1.0.0")
                        .description("노드 이벤트 API"))
                .servers(List.of(new Server().url("/").description("Gateway Server")));
    }

    @Bean
    public OpenApiCustomizer nodeGatewayPathCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }

            var paths = openApi.getPaths();

            if (paths.containsKey("/health")) {
                var healthPath = paths.remove("/health");
                paths.addPathItem("/node/health", healthPath);
            }

            if (paths.containsKey("/stomp/info")) {
                var stompPath = paths.remove("/stomp/info");
                paths.addPathItem("/node/stomp/info", stompPath);
            }
        };
    }
}
