package co.nilin.mixchange.port.api.postgres.dao

import co.nilin.mixchange.port.api.postgres.model.OrderModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.*

@Repository
interface OrderRepository : ReactiveCrudRepository<OrderModel, Long> {
    @Query("select * from orders where ouid = :ouid")
    fun findByOuid(@Param("ouid") ouid: String): Mono<OrderModel>

    @Query("select * from orders where symbol = :symbol and order_id = :orderId")
    fun findBySymbolAndOrderId(
            @Param("symbol")
            symbol: String, @Param("orderId")
            orderId: Long
    ): Mono<OrderModel>

    @Query("select * from orders where symbol = :symbol and client_order_id = :origClientOrderId")
    fun findBySymbolAndClientOrderId(
            @Param("symbol")
            symbol: String, @Param("origClientOrderId")
            origClientOrderId: String
    ): Mono<OrderModel>

    @Query("select * from orders where uuid = :uuid and (:symbol is null or symbol = :symbol) and status in (:statuses)")
    fun findByUuidAndSymbolAndStatus(
            @Param("uuid")
            uuid: String,
            @Param("symbol")
            symbol: String?, @Param("statuses")
            status: Collection<Int>
    ): Flow<OrderModel>

    @Query("select * from orders where uuid = :uuid " +
            "and (:symbol is null or symbol = :symbol) " +
            "and (:startTime is null or update_date >= :startTime)" +
            "and (:endTime is null or update_date < :endTime)"
    )
    fun findByUuidAndSymbolAndTimeBetween(
            @Param("uuid")
            uuid: String,
            @Param("symbol")
            symbol: String?,
            @Param("startTime")
            startTime: Date?,
            @Param("endTime")
            endTime: Date?
    ): Flow<OrderModel>
}