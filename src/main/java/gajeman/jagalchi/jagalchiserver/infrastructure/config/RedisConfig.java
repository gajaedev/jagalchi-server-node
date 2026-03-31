package gajeman.jagalchi.jagalchiserver.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import gajeman.jagalchi.jagalchiserver.domain.queue.ActionQueueItem;
import gajeman.jagalchi.jagalchiserver.domain.undo.UndoRedoManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        String host = System.getenv().getOrDefault("REDIS_HOST", "redis");
        int port = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, UndoRedoManager> undoRedoManagerRedisTemplate(LettuceConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<UndoRedoManager> serializer = new Jackson2JsonRedisSerializer<>(UndoRedoManager.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build(), ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(mapper);

        RedisTemplate<String, UndoRedoManager> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, ActionQueueItem> actionQueueRedisTemplate(LettuceConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<ActionQueueItem> serializer = new Jackson2JsonRedisSerializer<>(ActionQueueItem.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build(), ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(mapper);

        RedisTemplate<String, ActionQueueItem> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }
}
