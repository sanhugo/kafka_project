package ru.sanhugo.order_processor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = "spring.liquibase.enabled=false")
class OrderProcessorApplicationTests {

    // Create a mock KafkaTemplate specifically for tests.
    // This satisfies the dependency in KafkaConfig.errorHandler(KafkaOperations)
    @MockitoBean
    private KafkaTemplate<?, ?> kafkaTemplate;

    @Test
    void contextLoads() {
        // The test just checks that the context loads without errors
        // It now loads successfully because the dependency is satisfied by the mock
    }
}