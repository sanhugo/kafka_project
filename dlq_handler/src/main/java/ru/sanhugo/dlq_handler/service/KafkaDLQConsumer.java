package ru.sanhugo.dlq_handler.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import ru.sanhugo.common.Order;
import ru.sanhugo.common.OrderType;

@Service
@Slf4j
public class KafkaDLQConsumer {

    @KafkaListener(topics = "orders.DLT")
    public void listen(Order order, Acknowledgment ack) {
        if (order.getType()== OrderType.PURCHASE) {
            log.info("Recover purchase {}", order.getOrderId());
        }
        // ack-mode is manual: the offset is committed only after the record is handled
        ack.acknowledge();
    }

}
