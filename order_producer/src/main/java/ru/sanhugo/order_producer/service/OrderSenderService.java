package ru.sanhugo.order_producer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sanhugo.common.Order;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.sanhugo.common.OrderType;

import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSenderService {

    long counter = 1L;
    long customerId = 1L;
    boolean isPurchased = false;
    private final KafkaTemplate<String, Order> kafkaTemplate;
    Random random = new Random();

    @Scheduled(fixedRate = 2500)
    public void sendOrder(){
        long sequence = counter;
        Order order = createOrder(sequence);
        kafkaTemplate.send("orders", order.getOrderId(),order)
                .thenAccept(result -> log.info("Order {} (#{}) created and sent to broker", order.getOrderId(), sequence))
                .exceptionally(ex -> {
                    log.error("Error sending order {}", order.getOrderId(), ex);
                    return null;
                });
        counter++;
        customerId++;
        isPurchased^=true;
    }

    // The id is unique across producer restarts; the sequence number only selects the scenario
    // (sequence % 10 == 2: no amount; sequence % 10 == 5 or 8: TEMP_ERROR customer).
    private Order createOrder(long sequence){
        String orderId = "ORD-" + UUID.randomUUID();
        return switch ((int) (sequence%10)) {
            case 2 ->   Order.builder()
                        .orderId(orderId)
                        .customerId(String.valueOf(customerId%30))
                        .type(isPurchased ? OrderType.PURCHASE : OrderType.SUBSCRIPTION)
                        .totalAmount(null)
                        .build();
            case 5,8 -> Order.builder()
                        .orderId(orderId)
                        .customerId("TEMP_ERROR")
                        .type(isPurchased ? OrderType.PURCHASE : OrderType.SUBSCRIPTION)
                        .totalAmount(random.nextInt(20)+1)
                        .build();
            default ->  Order.builder()
                        .orderId(orderId)
                        .customerId(String.valueOf(customerId%30))
                        .type(isPurchased ? OrderType.PURCHASE : OrderType.SUBSCRIPTION)
                        .totalAmount(random.nextInt(20)+1)
                        .build();
        };
    }
}
