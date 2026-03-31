package gajeman.jagalchi.jagalchiserver.infrastructure.messaging.dto;

import java.time.LocalDate;

public record ActivityEvent(
        Long userId,
        LocalDate date
) {}
