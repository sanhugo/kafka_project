package ru.sanhugo.order_processor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = "spring.liquibase.enabled=false")
class OrderProcessorApplicationTests {

    // Создаем фиктивную KafkaTemplate специально для тестов.
    // Это удовлетворит зависимость в KafkaConfig.errorHandler(KafkaOperations)
    @MockitoBean
    private KafkaTemplate<?, ?> kafkaTemplate;

    @Test
    void contextLoads() {
        // Тест просто проверяет, что контекст загрузился без ошибок
        // Теперь он успешно загрузится, так как зависимость удовлетворена моком
    }
}