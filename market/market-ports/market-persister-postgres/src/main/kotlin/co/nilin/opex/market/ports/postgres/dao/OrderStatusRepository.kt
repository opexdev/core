package co.nilin.opex.market.ports.postgres.dao

import co.nilin.opex.market.ports.postgres.model.OrderStatusModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

@Repository
interface OrderStatusRepository : ReactiveCrudRepository<OrderStatusModel, Long> {

    @Query(
        """
        insert into order_status (ouid, executed_quantity, accumulative_quote_qty, status, appearance, date) 
        values (:ouid, :executedQuantity, :accumulativeQuoteQuantity, :status, :appearance, :date)
        on conflict do nothing
    """
    )
    fun insert(
        ouid: String,
        executedQuantity: BigDecimal,
        accumulativeQuoteQuantity: BigDecimal,
        status: Int,
        appearance: Int,
        date: LocalDateTime = LocalDateTime.now()
    ): Mono<Void>

    @Query(
        """
            with max_appearance as (select max(appearance) as max_app from order_status where ouid = :ouid)
            select * from order_status
            where ouid = :ouid
            and appearance = (select max_app from max_appearance)
            order by executed_quantity desc
            limit 1
        """
    )
    fun findMostRecentByOUID(ouid: String): Mono<OrderStatusModel>

}