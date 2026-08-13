package co.nilin.opex.market.ports.postgres.dao

import co.nilin.opex.market.core.inout.AggregatedOrderPriceModel
import co.nilin.opex.market.core.inout.MatchingOrderType
import co.nilin.opex.market.core.inout.OrderData
import co.nilin.opex.market.core.inout.OrderDirection
import co.nilin.opex.market.ports.postgres.model.OrderModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

import java.math.BigDecimal
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
        symbol: String,
        @Param("orderId")
        orderId: Long,
    ): Mono<OrderModel>

    @Query("select * from orders where symbol = :symbol and client_order_id = :origClientOrderId")
    fun findBySymbolAndClientOrderId(
        @Param("symbol")
        symbol: String,
        @Param("origClientOrderId")
        origClientOrderId: String,
    ): Mono<OrderModel>

    @Query("update orders set update_date = :updateDate where ouid = :ouid")
    fun touchUpdateDateByOuid(
        @Param("ouid")
        ouid: String,
        @Param("updateDate")
        updateDate: LocalDateTime = LocalDateTime.now()
    ): Mono<Void>

    @Query(
        """
        insert into orders (
            ouid, uuid, client_order_id, symbol, order_id,
            maker_fee, taker_fee, left_side_fraction, right_side_fraction,
            user_level, side, match_constraint, order_type,
            price, quantity, quote_quantity, create_date, update_date
        ) values (
            :ouid, :uuid, :clientOrderId, :symbol, :orderId,
            :makerFee, :takerFee, :leftSideFraction, :rightSideFraction,
            :userLevel, :side, :matchConstraint, :orderType,
            :price, :quantity, :quoteQuantity, :createDate, :updateDate
        )
        on conflict (ouid) do nothing
        returning ouid
        """
    )
    fun insertOrderIfAbsent(
        @Param("ouid")
        ouid: String,
        @Param("uuid")
        uuid: String,
        @Param("clientOrderId")
        clientOrderId: String?,
        @Param("symbol")
        symbol: String,
        @Param("orderId")
        orderId: Long?,
        @Param("makerFee")
        makerFee: BigDecimal?,
        @Param("takerFee")
        takerFee: BigDecimal?,
        @Param("leftSideFraction")
        leftSideFraction: BigDecimal?,
        @Param("rightSideFraction")
        rightSideFraction: BigDecimal?,
        @Param("userLevel")
        userLevel: String?,
        @Param("side")
        side: String?,
        @Param("matchConstraint")
        matchConstraint: String?,
        @Param("orderType")
        orderType: String?,
        @Param("price")
        price: BigDecimal?,
        @Param("quantity")
        quantity: BigDecimal?,
        @Param("quoteQuantity")
        quoteQuantity: BigDecimal?,
        @Param("createDate")
        createDate: LocalDateTime?,
        @Param("updateDate")
        updateDate: LocalDateTime
    ): Mono<String>

    @Query(
        """
        select * from orders
        join open_orders oo on orders.ouid = oo.ouid
        where uuid = :uuid and (:symbol is null or symbol = :symbol) and status in (:statuses)
        order by create_date desc
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
        limit: Int,
    ): Flow<OrderModel>

    @Query(
        """
        select * from orders where uuid = :uuid
            and (:symbol is null or symbol = :symbol)
            and (:startTime is null or update_date >= :startTime)
            and (:endTime is null or update_date < :endTime)
        order by update_date DESC 
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
        limit: Int,
    ): Flow<OrderModel>

    @Query(
        """
        select price, (sum(quantity) - sum(oo.executed_quantity)) as quantity from orders 
        join open_orders oo on orders.ouid = oo.ouid
        where symbol = :symbol and side = :direction and status in (:statuses) 
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
        status: Collection<Int>,
    ): Flux<AggregatedOrderPriceModel>

    @Query(
        """
        select price, (sum(quantity) - sum(oo.executed_quantity)) as quantity from orders 
        join open_orders oo on orders.ouid = oo.ouid
        where symbol = :symbol and side = :direction and status in (:statuses) 
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
        status: Collection<Int>,
    ): Flux<AggregatedOrderPriceModel>

    @Query("select * from orders where symbol = :symbol order by create_date desc limit 1")
    fun findLastOrderBySymbol(@Param("symbol") symbol: String): Mono<OrderModel>

    @Query("select count(distinct uuid) from orders where create_date >= :interval")
    fun countUsersWhoMadeOrder(interval: LocalDateTime): Flow<Long>

    @Query("select count(*) from orders where create_date >= :interval")
    fun countNewerThan(interval: LocalDateTime): Flow<Long>

    @Query("select count(*) from orders where symbol = :symbol and create_date >= :interval")
    fun countBySymbolNewerThan(interval: LocalDateTime, symbol: String): Flow<Long>

    @Query(
        """
with filtered_orders as (
    select o.symbol,
          o.ouid,
          o.order_type,
          o.side,
          o.price,
          o.quantity,
          o.quote_quantity,
          o.taker_fee,
          o.maker_fee,
          o.create_date,
          o.uuid
    from orders o
    where (:uuid is null or o.uuid = :uuid)
     and (:symbol is null or o.symbol = :symbol)
     and (:startTime is null or o.create_date >= :startTime)
     and (:endTime is null or o.create_date <= :endTime)
     and (:orderType is null or o.order_type = :orderType)
     and (:direction is null or o.side = :direction)
    order by o.create_date desc
    limit :limit offset :offset
)
select fo.symbol,
      fo.ouid,
      fo.order_type,
      fo.side,
      fo.price,
      fo.quantity,
      fo.quote_quantity,
      os.executed_quantity,
      fo.taker_fee,
      fo.maker_fee,
      os.status as status_code,
      os.appearance,
      fo.create_date,
      os.date as update_date,
      fo.uuid
from filtered_orders fo
left join lateral (
    select s.executed_quantity,
          s.status,
          s.appearance,
          s.date
    from order_status s
    where s.ouid = fo.ouid
    order by s.appearance desc, s.executed_quantity desc nulls last, s.date desc, s.id desc
    limit 1
) os on true
order by fo.create_date desc;
    """
    )
    fun findByCriteria(
        uuid: String?,
        symbol: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        orderType: MatchingOrderType?,
        direction: OrderDirection?,
        limit: Int?,
        offset: Int?,
    ): Flow<OrderData>

    @Query(
        """
select count(*)
from orders o
 WHERE (:uuid is null or o.uuid = :uuid)
   and (:symbol is null or o.symbol = :symbol)
   and (:startTime is null or o.create_date >= :startTime)
   and (:endTime is null or o.create_date <= :endTime)
   and (:orderType is null or o.order_type = :orderType)
   and (:direction is null or o.side = :direction)
    """
    )
    fun countByCriteria(
        uuid: String?,
        symbol: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        orderType: MatchingOrderType?,
        direction: OrderDirection?,
    ): Mono<Long>

    @Query("""
SELECT
    o.symbol ,
    o.ouid ,
    o.order_type ,
    o.side ,

    o.price AS price,
    o.quantity AS quantity,
    o.quote_quantity ,

    os.executed_quantity,

    o.taker_fee,
    o.maker_fee,

    os.status as status_code,
    os.appearance,

    o.create_date ,
    o.update_date,

    o.uuid

FROM orders o

LEFT JOIN (
    SELECT ranked.ouid,
           ranked.executed_quantity,
           ranked.status,
           ranked.appearance,
           ranked.date
    FROM (
             SELECT os.*,
                    ROW_NUMBER() OVER (
                        PARTITION BY os.ouid
                        ORDER BY os.appearance DESC, os.executed_quantity DESC NULLS LAST, os.date DESC, os.id DESC
                    ) AS rnk
             FROM order_status os
         ) ranked
    WHERE ranked.rnk = 1
) os
ON os.ouid = o.ouid

WHERE
    (:uuid IS NULL OR o.uuid = :uuid)
    AND (:symbol IS NULL OR o.symbol = :symbol)
    AND (:ouid IS NULL OR o.ouid = :ouid)
    AND (:fromDate IS NULL OR o.create_date >= :fromDate)
    AND (:toDate IS NULL OR o.create_date <= :toDate)
    AND (:orderType IS NULL OR o.order_type = :orderType)
    AND (:direction IS NULL OR o.side = :direction)

ORDER BY
CASE WHEN :ascendingByTime = true
     THEN o.create_date
END ASC,

CASE WHEN :ascendingByTime = false
     THEN o.create_date
END DESC

LIMIT :limit
OFFSET :offset
""")
    fun findRecentOrdersAdmin(
        uuid: String?,
        symbol: String?,
        ouid: String?,
        fromDate: LocalDateTime?,
        toDate: LocalDateTime?,
        orderType: MatchingOrderType?,
        direction: OrderDirection?,
        ascendingByTime: Boolean,
        limit: Int?,
        offset: Int?
    ): Flux<OrderData>
}