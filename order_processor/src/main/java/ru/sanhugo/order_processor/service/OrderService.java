package ru.sanhugo.order_processor.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sanhugo.common.Order;
import ru.sanhugo.order_processor.mapper.OrderMapper;
import ru.sanhugo.order_processor.repository.OrderRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderService {

    OrderRepository orderRepository;
    OrderMapper mapper;

    public void saveOrder(Order order) {
        ru.sanhugo.order_processor.entity.Order entity = mapper.toEntity(order);
        int inserted = orderRepository.insertIfAbsent(
                entity.getId(), entity.getCustomerId(), entity.getTotalAmount(), entity.getOrderType().name());
        if (inserted > 0) {
            log.info("Order {} Saved Successfully", order.getOrderId());
        } else {
            log.info("Order {} already exists, skipping duplicate", order.getOrderId());
        }
    }
}
