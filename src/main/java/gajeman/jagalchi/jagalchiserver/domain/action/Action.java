package gajeman.jagalchi.jagalchiserver.domain.action;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Action {
    private String actionId;
    private Long roadmapId;
    private Long actorId;
    private ActionType actionType;
    private Map<String, Object> payload;
    private Map<String, Object> prevState;
    private LocalDateTime clientTimestamp;
}

