package ru.sanhugo.dlq_handler.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.sanhugo.common.Order;
import ru.sanhugo.common.OrderType;

@Service
@Slf4j
public class KafkaDLQConsumer {

    @KafkaListener(topics = "orders.DLT")
    public void listen(Order order) {
        if (order.getType()== OrderType.PURCHASE) {
            log.info("Recover purchase {}", order.getOrderId());
        }
    }

}
