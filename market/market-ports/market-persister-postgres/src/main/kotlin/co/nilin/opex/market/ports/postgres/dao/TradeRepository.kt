package co.nilin.opex.market.ports.postgres.dao

import co.nilin.opex.market.core.inout.*
import co.nilin.opex.market.ports.postgres.model.*
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
    fun findByOuid(@Param("ouid") ouid: String): Flux<TradeModel>

    @Query("select * from trades where symbol = :symbol order by create_date desc limit 1")
    fun findMostRecentBySymbol(symbol: String): Flux<TradeModel>

    @Query("select * from trades where symbol = :symbol order by create_date desc limit :limit")
    fun findBySymbolSortDescendingByCreateDate(
        @Param("symbol")
        symbol: String,
        @Param("limit")
        limit: Int,
    ): Flux<TradeModel>

    @Query(
        """
            SELECT
                t.symbol AS symbol,
                t.base_asset AS baseAsset,
                t.quote_asset AS quoteAsset,
            
                t.trade_id AS id,
                t.matched_price AS price,
                t.matched_quantity AS quantity,
            
                CASE
                    WHEN mo.side = 'BID'
                    THEN mo.quote_quantity
                    ELSE to2.quote_quantity
                END AS quoteQuantity,
            
                t.create_date AS time,
            
                CASE
                    WHEN mo.side = 'BID'
                    THEN TRUE
                    ELSE FALSE
                END AS isMakerBuyer,
            
                NULL AS orderId,
                NULL AS commission,
                NULL AS commissionAsset,
                NULL AS isBuyer,
                NULL AS isMaker
            
            FROM trades t
            
            JOIN orders mo
                ON mo.ouid = t.maker_ouid
            
            JOIN orders to2
                ON to2.ouid = t.taker_ouid
            
            WHERE (:symbol IS NULL OR t.symbol = :symbol)
            
            ORDER BY t.trade_date DESC
            LIMIT :limit
     """
    )
    fun findRecentMarketTrades(
        @Param("symbol")
        symbol: String?,
        @Param("limit")
        limit: Int
    ): Flux<TradeUserContextProjection>

    @Query(
        """
                SELECT
                    t.symbol AS symbol,
                
                    NULL AS baseAsset,
                    NULL AS quoteAsset,
                
                    t.trade_id AS id,
                
                    CASE
                        WHEN t.taker_uuid = :uuid
                        THEN t.taker_price
                        ELSE t.maker_price
                    END AS price,
                
                    t.matched_quantity AS quantity,
                
                    CASE
                        WHEN mo.side = 'BID'
                        THEN mo.quote_quantity
                        ELSE to2.quote_quantity
                    END AS quoteQuantity,
                
                    t.create_date AS time,
                
                    CASE
                        WHEN mo.side = 'BID'
                        THEN TRUE
                        ELSE FALSE
                    END AS isMakerBuyer,
                
                    CASE
                        WHEN t.taker_uuid = :uuid
                        THEN to2.ouid
                        ELSE mo.ouid
                    END AS ouid,
                
                    CASE
                        WHEN t.taker_uuid = :uuid
                        THEN t.taker_commission
                        ELSE t.maker_commission
                    END AS commission,
                
                    CASE
                        WHEN t.taker_uuid = :uuid
                        THEN t.taker_commission_asset
                        ELSE t.maker_commission_asset
                    END AS commissionAsset,
                
                    CASE
                        WHEN t.taker_uuid = :uuid
                        THEN to2.side = 'ASK'
                        ELSE mo.side = 'ASK'
                    END AS isBuyer,
                
                    CASE
                        WHEN t.maker_uuid = :uuid
                        THEN TRUE
                        ELSE FALSE
                    END AS isMaker
                
                FROM trades t
                
                JOIN orders mo
                    ON mo.ouid = t.maker_ouid
                
                JOIN orders to2
                    ON to2.ouid = t.taker_ouid
                
                WHERE :uuid IN (t.taker_uuid, t.maker_uuid)
                    AND (:fromTrade IS NULL OR t.id > :fromTrade)
                    AND (:symbol IS NULL OR t.symbol = :symbol)
                    AND (:startTime IS NULL OR t.trade_date >= :startTime)
                    AND (:endTime IS NULL OR t.trade_date < :endTime)
                
                ORDER BY t.trade_date DESC
                LIMIT :limit
"""
    )
    fun findTradesWithUserContext(
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
        @Param("limit")
        limit: Int
    ): Flux<TradeUserContextProjection>


    @Query(
        """
        with first_trade as (select id, symbol, matched_price, matched_quantity from trades where id in (select min(id) from trades where create_date > :date group by symbol)),
            last_trade as (select id, symbol, matched_price, matched_quantity from trades where id in (select max(id) from trades where create_date > :date group by symbol))
        select symbol, 
        (select matched_price from last_trade where symbol=t.symbol) - (select matched_price from first_trade where symbol=t.symbol) as price_change,
        ((((select matched_price from last_trade where symbol=t.symbol) - (select matched_price from first_trade where symbol=t.symbol))/(select matched_price from first_trade where symbol=t.symbol))*100) as price_change_percent, 
        (sum(matched_quantity)/sum(matched_price)) as weighted_avg_price,
        (select matched_price from last_trade where symbol=t.symbol) as last_price, 
        (select matched_quantity from last_trade where symbol=t.symbol) as last_qty, 
        (
            select price from orders
            inner join open_orders oo on orders.ouid = oo.ouid
            where create_date > :date and symbol=t.symbol and side='BID'
            order by create_date desc limit 1
        ) as bid_price,
        (
            select price from orders
            inner join open_orders oo on orders.ouid = oo.ouid
            where create_date > :date and symbol=t.symbol and side='ASK'
            order by create_date desc limit 1
        ) as ask_price,
        (
            select price from orders
            inner join open_orders oo on orders.ouid = oo.ouid
            where create_date > :date and symbol=t.symbol
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
            inner join open_orders oo on orders.ouid = oo.ouid
            where create_date > :date and symbol=t.symbol and side='BID'
            order by create_date desc limit 1
        ) as bid_price,
        (
            select price from orders
            inner join open_orders oo on orders.ouid = oo.ouid
            where create_date > :date and symbol=t.symbol and side='ASK'
            order by create_date desc limit 1
        ) as ask_price,
        (
            select price from orders
            inner join open_orders oo on orders.ouid = oo.ouid
            where create_date > :date and symbol=t.symbol
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
                inner join open_orders oo on orders.ouid = oo.ouid
                where symbol = t.symbol and side='BID'
                order by create_date desc limit 1
            ) as bid_price,
            (
                select price from orders
                inner join open_orders oo on orders.ouid = oo.ouid
                where symbol = t.symbol and side='ASK'
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
                inner join open_orders oo on orders.ouid = oo.ouid
                where symbol = t.symbol and side='BID'
                order by create_date desc limit 1
            ) as bid_price,
            (
                select price from orders
                inner join open_orders oo on orders.ouid = oo.ouid
                where symbol = t.symbol and side='ASK'
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
                inner join open_orders oo on orders.ouid = oo.ouid
                where symbol = t.symbol and side='BID'
                order by create_date desc limit 1
            ) as bid_price,
            (
                select price from orders
                inner join open_orders oo on orders.ouid = oo.ouid
                where symbol = t.symbol and side='ASK'
                order by create_date limit 1
            ) as ask_price
            from trades as t 
            where symbol = :symbol
            group by symbol
        """
    )
    fun bestAskAndBidPrice(symbol: String): Mono<BestPrice>

    @Query("select symbol, matched_price from trades where create_date in (select max(create_date) from trades group by symbol) and symbol = :symbol")
    fun findBySymbolGroupBySymbol(@Param("symbol") symbol: String): Flux<LastPrice>

    @Query("select symbol, matched_price from trades where create_date in (select max(create_date) from trades group by symbol)")
    fun findAllGroupBySymbol(): Flux<LastPrice>

    @Query(
        """
    WITH intervals AS (
        SELECT *
        FROM interval_generator(
            (:startTime)::TIMESTAMP WITHOUT TIME ZONE,
            (:endTime)::TIMESTAMP WITHOUT TIME ZONE,
            :interval::INTERVAL
        )
    ),
    first_trade AS (
        SELECT DISTINCT ON (f.start_time)
            f.start_time,
            f.end_time,
            t.matched_price AS open_price
        FROM intervals f
        LEFT JOIN trades t
            ON t.create_date >= f.start_time
           AND t.create_date < f.end_time
           AND t.symbol = :symbol
        ORDER BY f.start_time, t.create_date
    ),
    last_trade AS (
        SELECT DISTINCT ON (f.start_time)
            f.start_time,
            f.end_time,
            t.matched_price AS close_price
        FROM intervals f
        LEFT JOIN trades t
            ON t.create_date >= f.start_time
           AND t.create_date < f.end_time
           AND t.symbol = :symbol
        ORDER BY f.start_time, t.create_date DESC
    ),
    ohlcv AS (
        SELECT 
            i.start_time AS open_time,
            i.end_time   AS close_time,
            ft.open_price AS open,
            MAX(t.matched_price) AS high,
            MIN(t.matched_price) AS low,
            lt.close_price AS close,
            SUM(t.matched_quantity) AS volume,
            COUNT(t.id) AS trades
        FROM intervals i
        LEFT JOIN trades t
            ON t.create_date >= i.start_time
           AND t.create_date < i.end_time
           AND t.symbol = :symbol
        LEFT JOIN first_trade ft
            ON i.start_time = ft.start_time
        LEFT JOIN last_trade lt
            ON i.start_time = lt.start_time
        GROUP BY i.start_time, i.end_time, ft.open_price, lt.close_price
    )
    SELECT *
    FROM (
        SELECT *
        FROM ohlcv
        ORDER BY open_time DESC
        limit :limit
    ) sub
    ORDER BY open_time ASC
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
    fun countNewerThan(interval: LocalDateTime): Flux<Long>

    @Query("select count(*) from trades where symbol = :symbol and create_date >= :interval")
    fun countBySymbolNewerThan(interval: LocalDateTime, symbol: String): Flux<Long>

    @Query(
        """
        WITH first_trade AS (SELECT symbol, MIN(id) AS min_id FROM trades WHERE create_date > :since GROUP BY symbol),
        last_trade AS (SELECT symbol, MAX(id) AS max_id FROM trades WHERE create_date > :since GROUP BY symbol),
        first_trade_details AS (SELECT ft.symbol, t.matched_price AS first_price FROM first_trade ft JOIN trades t ON ft.min_id = t.id),
        last_trade_details AS (SELECT lt.symbol, t.matched_price AS last_price FROM last_trade lt JOIN trades t ON lt.max_id = t.id)
        SELECT
            t.symbol,
            COALESCE(ltd.last_price, 0.0) AS last_price,
            COALESCE(ltd.last_price - ftd.first_price, 0.0) AS price_change,
            COALESCE(((ltd.last_price - ftd.first_price) / ftd.first_price) * 100, 0.0) AS price_change_percent
        FROM trades t
        JOIN first_trade_details ftd ON t.symbol = ftd.symbol
        JOIN last_trade_details ltd ON t.symbol = ltd.symbol
        WHERE t.create_date > :since
        GROUP BY t.symbol, ftd.first_price, ltd.last_price
        ORDER BY price_change_percent DESC
        LIMIT :limit;
    """
    )
    fun findByMostIncreasedPrice(since: LocalDateTime, limit: Int): Flux<PriceStat>

    @Query(
        """
        WITH first_trade AS (SELECT symbol, MIN(id) AS min_id FROM trades WHERE create_date > :since GROUP BY symbol),
        last_trade AS (SELECT symbol, MAX(id) AS max_id FROM trades WHERE create_date > :since GROUP BY symbol),
        first_trade_details AS (SELECT ft.symbol, t.matched_price AS first_price FROM first_trade ft JOIN trades t ON ft.min_id = t.id),
        last_trade_details AS (SELECT lt.symbol, t.matched_price AS last_price FROM last_trade lt JOIN trades t ON lt.max_id = t.id)
        SELECT
            t.symbol,
            COALESCE(ltd.last_price, 0.0) AS last_price,
            COALESCE(ltd.last_price - ftd.first_price, 0.0) AS price_change,
            COALESCE(((ltd.last_price - ftd.first_price) / ftd.first_price) * 100, 0.0) AS price_change_percent
        FROM trades t
        JOIN first_trade_details ftd ON t.symbol = ftd.symbol
        JOIN last_trade_details ltd ON t.symbol = ltd.symbol
        WHERE t.create_date > :since
        GROUP BY t.symbol, ftd.first_price, ltd.last_price
        ORDER BY price_change_percent
        LIMIT :limit;
    """
    )
    fun findByMostDecreasedPrice(since: LocalDateTime, limit: Int): Flux<PriceStat>

    @Query(
        """
        with first_trade as (select symbol, matched_quantity mq from trades where id in (select min(id) from trades where create_date > :since group by symbol)),
             last_trade as (select  symbol, matched_quantity mq from trades where id in (select max(id) from trades where create_date > :since group by symbol))
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
        with first_trade as (select symbol, matched_quantity mq from trades where id in (select min(id) from trades where create_date > :since group by symbol)),
             last_trade as (select  symbol, matched_quantity mq from trades where id in (select max(id) from trades where create_date > :since group by symbol))
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


    @Query(
        """ select t.trade_date As create_date,
            t.matched_quantity AS volume,
            t.matched_price AS matched_price,
            CASE
            WHEN t.maker_uuid = :user THEN t.maker_commission
            WHEN t.taker_uuid = :user THEN t.taker_commission
            END AS fee,
            CASE
            WHEN t.maker_uuid = :user THEN o1.side
            WHEN t.taker_uuid = :user THEN o2.side
            END AS side,
            t.matched_price * t.matched_quantity as transaction_price,
            substring(t.symbol, 0, position('_' in t.symbol) ) AS symbol
            FROM trades t
            INNER JOIN orders o1 ON t.maker_ouid = o1.ouid
            LEFT JOIN orders o2 ON t.taker_ouid = o2.ouid
            WHERE (t.maker_uuid = :user OR t.taker_uuid = :user)
            and (:startDate is null or trade_date >=:startDate) 
            and (:endDate is null or trade_date <=:endDate)
            
            union 
            
            select t.trade_date As create_date,
            t.matched_quantity AS volume,
            t.matched_price AS matched_price,
            CASE
            WHEN t.taker_uuid = :user THEN t.taker_commission
            WHEN t.maker_uuid = :user THEN t.maker_commission
            END AS fee,
            CASE
            WHEN t.taker_uuid = :user THEN o2.side
            WHEN t.maker_uuid = :user THEN o1.side
            END AS side,
            t.matched_price * t.matched_quantity as transaction_price,
            substring(t.symbol, 0, position('_' in t.symbol) ) AS symbol
            FROM trades t
            INNER JOIN orders o1 ON t.maker_ouid = o1.ouid
            LEFT JOIN orders o2 ON t.taker_ouid = o2.ouid
            WHERE (t.maker_uuid = :user OR t.taker_uuid = :user)
            and (:startDate is null or trade_date >=:startDate) 
            and (:endDate is null or trade_date <=:endDate)            
            
            order by create_date ASC offset :offset limit :limit  """
    )

    fun findTxOfTradesAsc(
        user: String,
        startDate: LocalDateTime?,
        endDate: LocalDateTime?,
        offset: Int?,
        limit: Int?,
    ): Flux<Transaction>


    @Query(
        """ select t.trade_date As create_date,
            t.matched_quantity AS volume,
            t.matched_price AS matched_price,
            CASE
            WHEN t.maker_uuid = :user THEN t.maker_commission
            WHEN t.taker_uuid = :user THEN t.taker_commission
            END AS fee,
            CASE
            WHEN t.maker_uuid = :user THEN o1.side
            WHEN t.taker_uuid = :user THEN o2.side
            END AS side,
            t.matched_price * t.matched_quantity as transaction_price,
            substring(t.symbol, 0, position('_' in t.symbol) ) AS symbol
            FROM trades t
            INNER JOIN orders o1 ON t.maker_ouid = o1.ouid
            LEFT JOIN orders o2 ON t.taker_ouid = o2.ouid
            WHERE (t.maker_uuid = :user OR t.taker_uuid = :user)
            and (:startDate is null or trade_date >=:startDate) 
            and (:endDate is null or trade_date <=:endDate)
            
            union 
            
            select t.trade_date As create_date,
            t.matched_quantity AS volume,
            t.matched_price AS matched_price,
            CASE
            WHEN t.taker_uuid = :user THEN t.taker_commission
            WHEN t.maker_uuid = :user THEN t.maker_commission
            END AS fee,
            CASE
            WHEN t.taker_uuid = :user THEN o2.side
            WHEN t.maker_uuid = :user THEN o1.side
            END AS side,
            t.matched_price * t.matched_quantity as transaction_price,
            substring(t.symbol, 0, position('_' in t.symbol) ) AS symbol
            FROM trades t
            INNER JOIN orders o1 ON t.maker_ouid = o1.ouid
            LEFT JOIN orders o2 ON t.taker_ouid = o2.ouid
            WHERE (t.maker_uuid = :user OR t.taker_uuid = :user)
            and (:startDate is null or trade_date >=:startDate) 
            and (:endDate is null or trade_date <=:endDate)            
            
            order by create_date DESC offset :offset limit :limit  """
    )

    fun findTxOfTradesDesc(
        user: String,
        startDate: LocalDateTime?,
        endDate: LocalDateTime?,
        offset: Int?,
        limit: Int?,
    ): Flux<Transaction>

    @Query(
        """
         WITH intervals AS (SELECT * FROM interval_generator((:startTime), (:endTime), :interval ::INTERVAL)),
        last_trade AS (
            SELECT DISTINCT ON (f.start_time) f.start_time,  f.end_time,  t.matched_price AS close_price FROM intervals f
            LEFT JOIN trades t ON t.create_date >= f.start_time AND t.create_date < f.end_time AND t.symbol = :symbol
            ORDER BY f.start_time, t.create_date DESC
        )
        SELECT
            i.end_time AS close_time,
            lt.close_price AS close_price
        FROM intervals i
        LEFT JOIN trades t
        ON t.create_date >= i.start_time AND t.create_date < i.end_time AND t.symbol = :symbol
        LEFT JOIN last_trade lt
        ON i.start_time = lt.start_time
        GROUP BY i.start_time, i.end_time, lt.close_price
        ORDER BY i.start_time;
        """
    )
    suspend fun getPriceTimeData(
        @Param("symbol")
        symbol: String,
        @Param("interval")
        interval: String,
        @Param("startTime")
        startTime: LocalDateTime,
        @Param("endTime")
        endTime: LocalDateTime,
    ): Flux<PriceTimeData>


    @Query(
        """
        select * from trades where
             (:symbol is null or symbol = :symbol) 
            and (:makerUuid is null or maker_uuid = :makerUuid) 
            and (:takerUuid is null or taker_uuid = :takerUuid) 
            and (:fromDate is null or trade_date >= :fromDate) 
            and (:toDate is null or trade_date <= :toDate) 
            and (:excludeSelfTrade is false or maker_uuid != taker_uuid)
        order by trade_date DESC 
        limit :limit
        offset :offset
        """
    )
    suspend fun findByCriteria(
        symbol: String?,
        makerUuid: String?,
        takerUuid: String?,
        fromDate: LocalDateTime?,
        toDate: LocalDateTime?,
        excludeSelfTrade: Boolean,
        limit: Int,
        offset: Int,
    ): Flux<TradeModel>

    @Query(
        """
        select * from trades where
            (:symbol is null or symbol=:symbol)
            and  (:baseAsset is null or base_asset = :baseAsset)
            and (:quoteAsset is null or quote_asset = :quoteAsset)
            and (:uuid is null or :uuid in (maker_uuid, taker_uuid))
            and (:makerUuid is null or maker_uuid = :makerUuid) 
            and (:takerUuid is null or taker_uuid = :takerUuid) 
            and (:ouid is null or :ouid in (maker_ouid, taker_ouid))
            and (:makerOuid is null or maker_ouid = :makerOuid) 
            and (:takerOuid is null or taker_ouid = :takerOuid) 
            and (:fromDate is null or trade_date >= :fromDate) 
            and (:toDate is null or trade_date <= :toDate) 
            and (:excludeSelfTrade is false or maker_uuid != taker_uuid)
        order by trade_date ASC 
        limit :limit
        offset :offset
        """
    )
    suspend fun findByCriteriaByBaseQuoteAsc(
        symbol: String?,
        baseAsset: String?,
        quoteAsset: String?,
        uuid: String?,
        makerUuid: String?,
        takerUuid: String?,
        ouid: String?,
        makerOuid: String?,
        takerOuid: String?,
        fromDate: LocalDateTime?,
        toDate: LocalDateTime?,
        excludeSelfTrade: Boolean,
        limit: Int? = 10,
        offset: Int? = 0,
    ): Flux<TradeModel>

    @Query(
        """
        select * from trades where
             (:symbol is null or symbol=:symbol)
            and (:baseAsset is null or base_asset = :baseAsset)
            and (:quoteAsset is null or quote_asset = :quoteAsset)
            and (:uuid is null or :uuid in (maker_uuid, taker_uuid))
            and (:makerUuid is null or maker_uuid = :makerUuid) 
            and (:takerUuid is null or taker_uuid = :takerUuid) 
            and (:ouid is null or :ouid in (maker_ouid, taker_ouid))
            and (:makerOuid is null or maker_ouid = :makerOuid) 
            and (:takerOuid is null or taker_ouid = :takerOuid) 
            and (:fromDate is null or trade_date >= :fromDate) 
            and (:toDate is null or trade_date <= :toDate) 
            and (:excludeSelfTrade is false or maker_uuid != taker_uuid)
        order by trade_date DESC 
        limit :limit
        offset :offset
        """
    )
    suspend fun findByCriteriaByBaseQuoteDesc(
        symbol: String?,
        baseAsset: String?,
        quoteAsset: String?,
        uuid: String?,
        makerUuid: String?,
        takerUuid: String?,
        ouid: String?,
        makerOuid: String?,
        takerOuid: String?,
        fromDate: LocalDateTime?,
        toDate: LocalDateTime?,
        excludeSelfTrade: Boolean,
        limit: Int? = 10,
        offset: Int? = 0,
    ): Flux<TradeModel>


    @Query(
        """
select t.symbol,
       t.id,
       o.order_id,
       case when :uuid = t.maker_uuid then t.maker_price else t.taker_price end           as price,
       t.matched_quantity as quantity,
       o.quote_quantity,
       case when :uuid = t.maker_uuid then t.maker_commission else t.taker_commission end as commission,
       case
           when :uuid = t.maker_uuid then t.maker_commission_asset
           else t.taker_commission_asset end                                              as commission_asset,
       t.trade_date as time,
       o.side = 'BID'                                                                     as is_buyer,
       t.maker_uuid = :uuid                                                               as is_maker,
       true                                                                               as is_best_match,
       case when o.side = 'BID' and t.maker_uuid = :uuid then true else false end         as is_maker_buyer
from trades t
         inner join orders o on
        (t.maker_uuid = :uuid and o.ouid = t.maker_ouid) or
        (t.taker_uuid = :uuid and o.ouid = t.taker_ouid)
where (:uuid is null or :uuid in (t.maker_uuid, t.taker_uuid))
            and (:symbol is null or t.symbol = :symbol) 
            and (:startTime is null or t.trade_date >= :startTime) 
            and (:endTime is null or t.trade_date <= :endTime) 
            and (:direction is null or o.side = :direction)
        order by t.trade_date desc 
        limit :limit
        offset :offset
        """
    )
    suspend fun findByCriteria(
        uuid: String?,
        symbol: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        direction: OrderDirection?,
        limit: Int?,
        offset: Int?,
    ): Flux<Trade>

    @Query(
        """
select count(*)
from trades t
         inner join orders o on
        (t.maker_uuid = :uuid and o.ouid = t.maker_ouid) or
        (t.taker_uuid = :uuid and o.ouid = t.taker_ouid)
where (:uuid is null or :uuid in (t.maker_uuid, t.taker_uuid) )   
            and (:symbol is null or t.symbol = :symbol) 
            and (:startTime is null or t.trade_date >= :startTime) 
            and (:endTime is null or t.trade_date <= :endTime) 
            and (:direction is null or o.side = :direction)
        """
    )
    suspend fun countByCriteria(
        uuid: String?,
        symbol: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        direction: OrderDirection?,
    ): Mono<Long>
}