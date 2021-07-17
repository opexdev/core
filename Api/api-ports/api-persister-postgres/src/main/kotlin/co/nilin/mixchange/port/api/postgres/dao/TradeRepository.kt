package co.nilin.mixchange.port.api.postgres.dao

import co.nilin.mixchange.port.api.postgres.model.TradeModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TradeRepository : ReactiveCrudRepository<TradeModel, Long> {

    @Query("select * from trades where :ouid in (taker_ouid, maker_ouid) ")
    fun findByOuid(@Param("ouid") ouid: String): Flow<TradeModel>

    @Query("select * from trades where :uuid in (taker_uuid, maker_uuid) " +
            "and (:fromTrade is null or id > :fromTrade) " +
            "and (:symbol is null or symbol = :symbol) " +
            "and (:startTime is null or trade_date >= :startTime) " +
            "and (:endTime is null or trade_date < :endTime)"
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
            endTime: Date?
    ): Flow<TradeModel>
}