package ru.sanhugo.order_processor.repository;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sanhugo.order_processor.entity.Order;

@Repository
public interface OrderRepository extends CrudRepository<Order, String> {

    /**
     * Idempotent insert. The order id is assigned by the producer, so save() would treat the entity
     * as existing and run an UPDATE that silently affects 0 rows. A redelivered record is a no-op here.
     *
     * @return 1 if the order was inserted, 0 if it already existed
     */
    @Modifying
    @Query("""
            INSERT INTO orders (id, customer_id, total_amount, order_type)
            VALUES (:id, :customerId, :totalAmount, :orderType)
            ON CONFLICT (id) DO NOTHING
            """)
    int insertIfAbsent(@Param("id") String id,
                       @Param("customerId") String customerId,
                       @Param("totalAmount") Integer totalAmount,
                       @Param("orderType") String orderType);
}
