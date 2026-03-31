package gajeman.jagalchi.jagalchiserver.infrastructure.messaging;

import gajeman.jagalchi.jagalchiserver.infrastructure.messaging.dto.ActivityEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityEventPublisher {

    private static final String ACTIVITY_QUEUE = "activity.queue";
    
    private final RabbitTemplate rabbitTemplate;

    public void publishActivityEvent(Long userId, LocalDate date) {
        try {
            ActivityEvent event = new ActivityEvent(userId, date);
            rabbitTemplate.convertAndSend(ACTIVITY_QUEUE, event);
            log.info("Published ActivityEvent for userId: {}, date: {}", userId, date);
        } catch (Exception e) {
            // 메시지 발행 실패는 로깅만 하고 주요 기능에 영향을 주지 않음
            log.warn("Failed to publish ActivityEvent for userId: {}, date: {}. Error: {}", 
                    userId, date, e.getMessage());
        }
    }
}
