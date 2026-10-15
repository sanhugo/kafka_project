package ru.sanhugo.order_processor.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sanhugo.order_processor.entity.Order;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(source = "orderId", target = "id")
    @Mapping(source = "type", target = "orderType")
    Order toEntity(ru.sanhugo.common.Order order);
}
