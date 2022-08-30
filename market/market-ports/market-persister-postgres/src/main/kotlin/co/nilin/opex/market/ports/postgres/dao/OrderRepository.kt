package co.nilin.opex.market.ports.postgres.dao

import co.nilin.opex.market.core.inout.AggregatedOrderPriceModel
import co.nilin.opex.market.core.inout.OrderDirection
import co.nilin.opex.market.ports.postgres.model.OrderModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

@Repository
interface OrderRepository : ReactiveCrudRepository<OrderModel, Long> {

    @Query("select * from orders where ouid = :ouid")
    fun findByOuid(@Param("ouid") ouid: String): Mono<OrderModel>

    @Query("select * from orders where uuid = :uuid and ouid = :ouid")
    fun findByUUIDAndOUID(@Param("uuid") uuid: String, @Param("ouid") ouid: String): Mono<OrderModel>

    @Query("select * from orders where symbol = :symbol and order_id = :orderId")
    fun findBySymbolAndOrderId(
        @Param("symbol")
        symbol: String, @Param("orderId")
        orderId: Long
    ): Mono<OrderModel>

    @Query("select * from orders where symbol = :symbol and client_order_id = :origClientOrderId")
    fun findBySymbolAndClientOrderId(
        @Param("symbol")
        symbol: String,
        @Param("origClientOrderId")
        origClientOrderId: String
    ): Mono<OrderModel>

    @Query(
        """
        select * from orders
        join order_status os on orders.ouid = os.ouid
        where uuid = :uuid and (:symbol is null or symbol = :symbol) and status in (:statuses)
            and appearance = (select max(appearance) from order_status where ouid = orders.ouid)
            and executed_quantity = (select max(executed_quantity) from order_status where ouid = orders.ouid)
        limit :limit
    """
    )
    fun findByUuidAndSymbolAndStatus(
        @Param("uuid")
        uuid: String,
        @Param("symbol")
        symbol: String?,
        @Param("statuses")
        status: Collection<Int>,
        limit: Int
    ): Flow<OrderModel>

    @Query(
        """
        select * from orders where uuid = :uuid
            and (:symbol is null or symbol = :symbol)
            and (:startTime is null or update_date >= :startTime)
            and (:endTime is null or update_date < :endTime)
        limit :limit
        """
    )
    fun findByUuidAndSymbolAndTimeBetween(
        @Param("uuid")
        uuid: String,
        @Param("symbol")
        symbol: String?,
        @Param("startTime")
        startTime: Date?,
        @Param("endTime")
        endTime: Date?,
        limit: Int
    ): Flow<OrderModel>

    @Query(
        """
        select price, (sum(quantity) - sum(os.executed_quantity)) as quantity from orders 
        join order_status os on orders.ouid = os.ouid
        where symbol = :symbol and side = :direction and os.status in (:statuses) 
            and appearance = (select max(appearance) from order_status where ouid = orders.ouid)
            and executed_quantity = (select max(executed_quantity) from order_status where ouid = orders.ouid)
        group by price 
        order by price asc 
        limit :limit
    """
    )
    fun findBySymbolAndDirectionAndStatusSortAscendingByPrice(
        @Param("symbol")
        symbol: String,
        @Param("direction")
        direction: OrderDirection,
        @Param("limit")
        limit: Int,
        @Param("statuses")
        status: Collection<Int>
    ): Flux<AggregatedOrderPriceModel>

    @Query(
        """
        select price, (sum(quantity) - sum(executed_quantity)) as quantity from orders 
        join order_status os on orders.ouid = os.ouid
        where symbol = :symbol and side = :direction and status in (:statuses) 
            and appearance = (select max(appearance) from order_status where ouid = orders.ouid)
            and executed_quantity = (select max(executed_quantity) from order_status where ouid = orders.ouid)
        group by price 
        order by price desc
        limit :limit
    """
    )
    fun findBySymbolAndDirectionAndStatusSortDescendingByPrice(
        @Param("symbol")
        symbol: String,
        @Param("direction")
        direction: OrderDirection,
        @Param("limit")
        limit: Int,
        @Param("statuses")
        status: Collection<Int>
    ): Flux<AggregatedOrderPriceModel>

    @Query("select * from orders where symbol = :symbol order by create_date desc limit 1")
    fun findLastOrderBySymbol(@Param("symbol") symbol: String): Mono<OrderModel>

    @Query("select count(distinct uuid) from orders where create_date >= :interval")
    fun countUsersWhoMadeOrder(interval: LocalDateTime): Flow<Long>

    @Query("select count(*) from orders where create_date >= :interval")
    fun countNewerThan(interval: LocalDateTime): Flow<Long>

    @Query("select count(*) from orders where symbol = :symbol and create_date >= :interval")
    fun countBySymbolNewerThan(interval: LocalDateTime, symbol: String): Flow<Long>
}