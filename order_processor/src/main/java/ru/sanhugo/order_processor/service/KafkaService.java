package ru.sanhugo.order_processor.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import ru.sanhugo.common.Order;
import ru.sanhugo.order_processor.exception.OrderValidationException;
import ru.sanhugo.order_processor.exception.TempErrorException;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaService {
    OrderService service;

    @KafkaListener(topics = "orders")
    public void listen(Order order, Acknowledgment ack) {
        if (order.getTotalAmount() == null) {
            log.warn("Order {} has null totalAmount, sending to DLQ", order.getOrderId());
            throw new OrderValidationException("totalAmount is null for order " + order.getOrderId());
        } else if ("TEMP_ERROR".equals(order.getCustomerId())) {
            log.info("Server is down");
            throw new TempErrorException("Temporary server error");
        } else {
            service.saveOrder(order);
            // ack-mode is manual: the offset is committed only after the order is stored
            ack.acknowledge();
        }
    }
}
