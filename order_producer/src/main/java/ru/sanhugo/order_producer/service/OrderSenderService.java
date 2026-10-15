package ru.sanhugo.order_producer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sanhugo.common.Order;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.sanhugo.common.OrderType;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSenderService {

    long counter = 1L;
    long customerId = 1L;
    boolean isPurchased = false;
    private final KafkaTemplate<String, Order> kafkaTemplate;

    @Scheduled(fixedRate = 2500)
    public void sendOrder(){
        Order order = createOrder(counter);
        kafkaTemplate.send("orders", order.getOrderId(),order)
                .thenAccept(result -> {
                    log.info("Order {} created and sent to broker", order.getOrderId());
                })
                .exceptionally(ex -> {
                    log.error("Error sending order {}", order.getOrderId(), ex);
                    return null;
                });
        counter++;
        customerId++;
        isPurchased^=true;
    }

    private Order createOrder(long orderId){
        String order_id = "ORD-"+orderId;
        Order order;
        switch ((int) (orderId%10)) {
            case 2 -> {
                order = Order.builder()
                        .orderId(order_id)
                        .customerId(String.valueOf(customerId%30))
                        .type(isPurchased ? OrderType.PURCHASE : OrderType.SUBSCRIPTION)
                        .totalAmount(null)
                        .build();
            }
            case 5,8 -> {
                order = Order.builder()
                        .orderId(order_id)
                        .customerId("TEMP_ERROR")
                        .type(isPurchased ? OrderType.PURCHASE : OrderType.SUBSCRIPTION)
                        .totalAmount((int) (Math.random()*20+1))
                        .build();
            }
            default -> {
                order = Order.builder()
                        .orderId(order_id)
                        .customerId(String.valueOf(customerId%30))
                        .type(isPurchased ? OrderType.PURCHASE : OrderType.SUBSCRIPTION)
                        .totalAmount((int) (Math.random()*20+1))
                        .build();
            }

        }
        return order;
    }
}
