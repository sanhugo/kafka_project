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

                    // Business validation error: published to orders.DLT right away (no retry)
                    if (cause instanceof OrderValidationException) {
                        return new TopicPartition("orders.DLT", record.partition()%2);
                    }

                    // All other errors (e.g. TEMP_ERROR): no destination, so nothing is published to the DLT.
                    // The record is retried 3 times (see backOff below), then skipped and its offset is committed.
                    return null;
                };

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(kafkaOperations, destinationResolver);

        FixedBackOff backOff = new FixedBackOff(1500L, 3L);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

        // Validation errors are not retried: they go straight to the recoverer (and thus to the DLT)
        handler.addNotRetryableExceptions(OrderValidationException.class);

        return handler;
    }

}