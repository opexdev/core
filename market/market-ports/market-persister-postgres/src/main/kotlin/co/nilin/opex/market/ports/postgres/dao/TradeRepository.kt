package co.nilin.opex.market.ports.postgres.dao

import co.nilin.opex.market.core.inout.BestPrice
import co.nilin.opex.market.core.inout.PriceStat
import co.nilin.opex.market.core.inout.TradeVolumeStat
import co.nilin.opex.market.ports.postgres.model.CandleInfoData
import co.nilin.opex.market.ports.postgres.model.TradeModel
import co.nilin.opex.market.ports.postgres.model.TradeTickerData
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
interface TradeRepository : ReactiveCrudRepository<TradeModel, Long> {

    @Query("select * from trades where :ouid in (taker_ouid, maker_ouid)")
    fun findByOuid(@Param("ouid") ouid: String): Flow<TradeModel>

    @Query("select * from trades where symbol = :symbol order by create_date desc limit 1")
    fun findMostRecentBySymbol(symbol: String): Flow<TradeModel>

    @Query(
        """
        select * from trades where :uuid in (taker_uuid, maker_uuid) 
            and (:fromTrade is null or id > :fromTrade) 
            and (:symbol is null or symbol = :symbol) 
            and (:startTime is null or trade_date >= :startTime) 
            and (:endTime is null or trade_date < :endTime)
        limit :limit
        """
    )
    fun findByUuidAndSymbolAndTimeBetweenAndTradeIdGreaterThan(
        @Param("uuid")
        uuid: String,
        @Param("symbol")
        symbol: String?,
        @Param("fromTrade")
        fromTrade: Long?,
        @Param("startTime")
        startTime: Date?,
        @Param("endTime")
        endTime: Date?,
        limit: Int
    ): Flow<TradeModel>

    @Query("select * from trades where symbol = :symbol order by create_date desc limit :limit")
    fun findBySymbolSortDescendingByCreateDate(
        @Param("symbol")
        symbol: String,
        @Param("limit")
        limit: Int
    ): Flow<TradeModel>

    @Query(
        """
        with first_trade as (select * from trades where create_date > :date order by create_date limit 1),
             last_trade as (select * from trades where create_date > :date order by create_date desc limit 1)
        select symbol, 
        (select matched_price from last_trade where symbol=t.symbol) - (select matched_price from first_trade where symbol=t.symbol) as price_change,
        ((((select matched_price from last_trade where symbol=t.symbol) - (select matched_price from first_trade where symbol=t.symbol))/(select matched_price from first_trade where symbol=t.symbol))*100) as price_change_percent, 
        (sum(matched_quantity)/sum(matched_price)) as weighted_avg_price,
        (select matched_price from last_trade where symbol=t.symbol) as last_price, 
        (select matched_quantity from last_trade where symbol=t.symbol) as last_qty, 
        (
            select price from orders
            join order_status os on orders.ouid = os.ouid
            where create_date > :date and symbol=t.symbol and status in (1, 4) and side='BID' 
            and appearance = (select max(appearance) from order_status where ouid = orders.ouid)
            and executed_quantity = (select max(executed_quantity) from order_status where ouid = orders.ouid)
            order by create_date desc limit 1
        ) as bid_price,
        (
            select price from orders 
            join order_status os on orders.ouid = os.ouid
            where create_date > :date and symbol=t.symbol and status in (1, 4) and side='ASK' 
            and appearance = (select max(appearance) from order_status where ouid = orders.ouid)
            and executed_quantity = (select max(executed_quantity) from order_status where ouid = orders.ouid)
            order by create_date limit 1
        ) as ask_price,
        (
            select price from orders 
            join order_status os on orders.ouid = os.ouid
            where create_date > :date and symbol=t.symbol and status in (1, 4) 
            and appearance = (select max(appearance) from order_status where ouid = orders.ouid)
            and executed_quantity = (select max(executed_quantity) from order_status where ouid = orders.ouid)
            order by create_date desc limit 1
        ) as open_price,
        max(matched_price) as high_price, 
        min(matched_price) as low_price, 
        sum(matched_quantity) as volume, 
        (select id from first_trade where symbol=t.symbol) as first_id, 
        (select id from last_trade where symbol=t.symbol) as last_id, 
        count(id) as count
        from trades as t 
        where create_date > :date
        group by symbol
        """
    )
    fun tradeTicker(@Param("date") createDate: LocalDateTime): Flux<TradeTickerData>

    @Query(
        """
        with first_trade as (select * from trades where create_date > :date and symbol = :symbol order by create_date limit 1),
             last_trade as (select * from trades where create_date > :date and symbol = :symbol order by create_date desc limit 1)
        select symbol, 
        (select matched_price from last_trade) - (select matched_price from first_trade) as price_change,
        ((((select matched_price from last_trade) - (select matched_price from first_trade))/(select matched_price from first_trade))*100) as price_change_percent, 
        (sum(matched_quantity)/sum(matched_price)) as weighted_avg_price,
        (select matched_price from last_trade) as last_price, 
        (select matched_quantity from last_trade) as last_qty, 
        (
            select price from orders
            join order_status os on orders.ouid = os.ouid
            where create_date > :date and symbol=t.symbol and status in (1, 4) and side='BID' 
            and appearance = (select max(appearance) from order_status where ouid = orders.ouid)
            and executed_quantity = (select max(executed_quantity) from order_status where ouid = orders.ouid)
            order by create_date desc limit 1
        ) as bid_price,
        (
            select price from orders 
            join order_status os on orders.ouid = os.ouid
            where create_date > :date and symbol=t.symbol and status in (1, 4) and side='ASK' 
            and appearance = (select max(appearance) from order_status where ouid = orders.ouid)
            and executed_quantity = (select max(executed_quantity) from order_status where ouid = orders.ouid)
            order by create_date limit 1
        ) as ask_price,
        (
            select price from orders 
            join order_status os on orders.ouid = os.ouid
            where create_date > :date and symbol=t.symbol and status in (1, 4) 
            and appearance = (select max(appearance) from order_status where ouid = orders.ouid)
            and executed_quantity = (select max(executed_quantity) from order_status where ouid = orders.ouid)
            order by create_date desc limit 1
        ) as open_price,
        max(matched_price) as high_price, 
        min(matched_price) as low_price, 
        sum(matched_quantity) as volume, 
        (select id from first_trade) as first_id, 
        (select id from last_trade) as last_id, 
        count(id) as count
        from trades as t 
        where create_date > :date and symbol = :symbol
        group by symbol
        """
    )
    fun tradeTickerBySymbol(
        @Param("symbol")
        symbol: String,
        @Param("date")
        createDate: LocalDateTime,
    ): Mono<TradeTickerData>

    @Query(
        """
            select symbol, 
            (
                select price from orders
                join order_status os on orders.ouid = os.ouid
                where symbol = t.symbol and status in (1, 4) and side = 'BID' 
                and appearance = (select max(appearance) from order_status where ouid = orders.ouid)
                and executed_quantity = (select max(executed_quantity) from order_status where ouid = orders.ouid)
                order by create_date desc limit 1
            ) as bid_price,
            (
                select price from orders 
                join order_status os on orders.ouid = os.ouid
                where symbol = t.symbol and status in (1, 4) and side = 'ASK' 
                and appearance = (select max(appearance) from order_status where ouid = orders.ouid)
                and executed_quantity = (select max(executed_quantity) from order_status where ouid = orders.ouid)
                order by create_date limit 1
            ) as ask_price
            from trades as t
            group by symbol
        """
    )
    fun bestAskAndBidPrice(): Flux<BestPrice>

    @Query(
        """
            select symbol,
            (
                select price from orders
                join order_status os on orders.ouid = os.ouid
                where symbol = t.symbol and status in (1, 4) and side = 'BID' 
                and appearance = (select max(appearance) from order_status where ouid = orders.ouid)
                and executed_quantity = (select max(executed_quantity) from order_status where ouid = orders.ouid)
                order by create_date desc limit 1
            ) as bid_price,
            (
                select price from orders 
                join order_status os on orders.ouid = os.ouid
                where symbol = t.symbol and status in (1, 4) and side = 'ASK' 
                and appearance = (select max(appearance) from order_status where ouid = orders.ouid)
                and executed_quantity = (select max(executed_quantity) from order_status where ouid = orders.ouid)
                order by create_date limit 1
            ) as ask_price
            from trades as t 
            where symbol in (:symbols)
            group by symbol
        """
    )
    fun bestAskAndBidPrice(symbols: List<String>): Flux<BestPrice>

    @Query(
        """
            select symbol, 
            (
                select price from orders
                join order_status os on orders.ouid = os.ouid
                where symbol = t.symbol and status in (1, 4) and side = 'BID' 
                and appearance = (select max(appearance) from order_status where ouid = orders.ouid)
                and executed_quantity = (select max(executed_quantity) from order_status where ouid = orders.ouid)
                order by create_date desc limit 1
            ) as bid_price,
            (
                select price from orders 
                join order_status os on orders.ouid = os.ouid
                where symbol = t.symbol and status in (1, 4) and side = 'ASK' 
                and appearance = (select max(appearance) from order_status where ouid = orders.ouid)
                and executed_quantity = (select max(executed_quantity) from order_status where ouid = orders.ouid)
                order by create_date limit 1
            ) as ask_price
            from trades as t 
            where symbol = :symbol
            group by symbol
        """
    )
    fun bestAskAndBidPrice(symbol: String): Mono<BestPrice>

    @Query("select * from trades where create_date in (select max(create_date) from trades group by symbol) and symbol = :symbol")
    fun findBySymbolGroupBySymbol(@Param("symbol") symbol: String): Flux<TradeModel>

    @Query("select * from trades where create_date in (select max(create_date) from trades group by symbol)")
    fun findAllGroupBySymbol(): Flux<TradeModel>

    @Query(
        """
        with intervals as (select * from interval_generator((:startTime), (:endTime), :interval ::INTERVAL))
        select 
            f.start_time as open_time,
            f.end_time as close_time, 
            (select matched_price from trades tt where symbol = :symbol and tt.create_date >= f.start_time and tt.create_date < f.end_time order by tt.create_date limit 1) as open,
            max(t.matched_price) as high,
            min(t.matched_price) as low,
            (select matched_price from trades tt where symbol = :symbol and tt.create_date >= f.start_time and tt.create_date < f.end_time order by tt.create_date desc limit 1) as close,
            sum(t.matched_quantity) as volume,
            count(id) as trades
        from trades t
        right join intervals f
        on t.create_date >= f.start_time and t.create_date < f.end_time
        where symbol = :symbol or symbol is null
        group by f.start_time, f.end_time
        order by f.start_time
        limit :limit
        """
    )
    suspend fun candleData(
        @Param("symbol")
        symbol: String,
        @Param("interval")
        interval: String,
        @Param("startTime")
        startTime: LocalDateTime,
        @Param("endTime")
        endTime: LocalDateTime,
        @Param("limit")
        limit: Int,
    ): Flux<CandleInfoData>

    @Query("select * from trades order by create_date desc limit 1")
    suspend fun findLastByCreateDate(): Mono<TradeModel>

    @Query("select * from trades order by create_date limit 1")
    suspend fun findFirstByCreateDate(): Mono<TradeModel>

    @Query("select count(*) from trades where create_date >= :interval")
    fun countNewerThan(interval: LocalDateTime): Flow<Long>

    @Query("select count(*) from trades where symbol = :symbol and create_date >= :interval")
    fun countBySymbolNewerThan(interval: LocalDateTime, symbol: String): Flow<Long>

    @Query(
        """
        with first_trade as (select matched_price, symbol from trades where create_date > :since order by create_date limit 1),
             last_trade as (select matched_price, symbol from trades where create_date > :since order by create_date desc limit 1)
        select 
            symbol,
            coalesce((select matched_price from last_trade where symbol = t.symbol), 0.0) as last_price,
            coalesce(
                max(
                    (select matched_price from last_trade where symbol = t.symbol)
                  - (select matched_price from first_trade where symbol = t.symbol)
                ),
                0.0
            ) as price_change,
            coalesce(
                (
                    (select matched_price from last_trade where symbol = t.symbol)
                   -(select matched_price from first_trade where symbol = t.symbol)
                ) / (select matched_price from first_trade where symbol = t.symbol) * 100,
                0.0
            ) as price_change_percent
        from trades t
        group by symbol
        order by price_change_percent desc
        limit :limit
    """
    )
    fun findByMostIncreasedPrice(since: LocalDateTime, limit: Int): Flux<PriceStat>

    @Query(
        """
        with first_trade as (select matched_price, symbol from trades where create_date > :since order by create_date limit 1),
             last_trade as (select matched_price, symbol from trades where create_date > :since order by create_date desc limit 1)
        select 
            symbol,
            coalesce((select matched_price from last_trade where symbol = t.symbol), 0.0) as last_price,
            coalesce(
                max(
                    (select matched_price from last_trade where symbol = t.symbol)
                  - (select matched_price from first_trade where symbol = t.symbol)
                ),
                0.0
            ) as price_change,
            coalesce(
                (
                    (select matched_price from last_trade where symbol = t.symbol)
                   -(select matched_price from first_trade where symbol = t.symbol)
                ) / (select matched_price from first_trade where symbol = t.symbol) * 100,
                0.0
            ) as price_change_percent
        from trades t
        group by symbol
        order by price_change_percent
        limit :limit
    """
    )
    fun findByMostDecreasedPrice(since: LocalDateTime, limit: Int): Flux<PriceStat>

    @Query(
        """
        with first_trade as (select matched_quantity as mq, symbol from trades where create_date > :since order by create_date limit 1),
             last_trade as (select matched_quantity as mq, symbol from trades where create_date > :since order by create_date desc limit 1)
        select 
            symbol, 
            coalesce(sum(matched_quantity), 0.0) as volume, 
            count(id) as trade_count,
            coalesce(
                (
                    (select mq from last_trade where symbol = t.symbol)
                  - (select mq from first_trade where symbol = t.symbol)
                ) / (select mq from first_trade where symbol = t.symbol) * 100,
                0.0
            ) as change
        from trades t
        where create_date > :since
        group by symbol
        order by volume
        limit 1
    """
    )
    fun findByMostVolume(since: LocalDateTime): Mono<TradeVolumeStat>

    @Query(
        """
        with first_trade as (select matched_quantity as mq, symbol from trades where create_date > :since order by create_date limit 1),
             last_trade as (select matched_quantity as mq, symbol from trades where create_date > :since order by create_date desc limit 1)
        select 
            symbol, 
            coalesce(sum(matched_quantity), 0.0) as volume, 
            count(id) as trade_count,
            coalesce(
                (
                    (select mq from last_trade where symbol = t.symbol)
                  - (select mq from first_trade where symbol = t.symbol)
                ) / (select mq from first_trade where symbol = t.symbol) * 100,
                0.0
            ) as change
        from trades t
        where create_date > :since
        group by symbol
        order by trade_count
        limit 1
    """
    )
    fun findByMostTrades(since: LocalDateTime): Mono<TradeVolumeStat>
}