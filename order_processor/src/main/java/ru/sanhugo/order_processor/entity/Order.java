package ru.sanhugo.order_processor.entity;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import ru.sanhugo.common.OrderType;

@Getter
@Setter
@Table(name = "orders")
public class Order {

    @Id
    private String id;

    @Column("customer_id")
    private String customerId;

    @Column("total_amount")
    private Integer totalAmount;

    @Column("order_type")
    private OrderType orderType;
}
