package co.nilin.opex.market.ports.postgres.dao

import co.nilin.opex.market.ports.postgres.model.OpenOrderModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Repository
interface OpenOrderRepository : ReactiveCrudRepository<OpenOrderModel, Long> {

    @Query(
        """
        insert into open_orders (ouid, executed_quantity, status)
        values (:ouid, :executedQuantity, :status)
        on conflict (ouid) 
        do update set executed_quantity = excluded.executed_quantity, status = :status
    """
    )
    fun insertOrUpdate(ouid: String, executedQuantity: BigDecimal?, status: Int): Mono<Void>

    @Query("update open_orders set executed_quantity = :executedQuantity, status = :status where ouid = :ouid")
    fun update(ouid: String, executedQuantity: BigDecimal?): Mono<Void>

    @Query("delete from open_orders where ouid = :ouid")
    fun delete(ouid: String): Mono<Void>

}