package ru.sanhugo.order_processor.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;
import ru.sanhugo.order_processor.exception.OrderValidationException;

import java.util.function.BiFunction;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic dlqTopic() {
        return TopicBuilder.name("orders.DLT")
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public CommonErrorHandler errorHandler(KafkaOperations<Object, Object> kafkaOperations) {

        BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition> destinationResolver =
                (record, exception) -> {

                    Throwable cause = exception.getCause();

                    // Если это бизнес-ошибка валидации — сразу в DLQ (без retry)
                    if (cause instanceof OrderValidationException) {
                        return new TopicPartition("orders.DLT", record.partition()%2);
                    }

                    // Для всех остальных ошибок — тоже в DLQ (после исчерпания retry)
                    return null;
                };

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(kafkaOperations, destinationResolver);

        FixedBackOff backOff = new FixedBackOff(1500L, 3L);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

        // Исключения, которые НЕ нужно ретраить — сразу в DLQ
        handler.addNotRetryableExceptions(OrderValidationException.class);

        return handler;
    }

}