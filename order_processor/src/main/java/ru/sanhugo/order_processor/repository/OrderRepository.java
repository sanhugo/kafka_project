package ru.sanhugo.order_processor.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.sanhugo.order_processor.entity.Order;

@Repository
public interface OrderRepository extends CrudRepository<Order, String> {

}
