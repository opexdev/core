package co.nilin.opex.price.ports.postgres.dao

import co.nilin.opex.price.ports.postgres.model.RateHistoryModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface RateHistoryRepository : ReactiveCrudRepository<RateHistoryModel, Long> {

    @Query("SELECT * FROM rate_history WHERE symbol = :symbol ORDER BY created_date DESC LIMIT :limit")
    fun findBySymbolWithLimit(symbol: String, limit: Int): Flux<RateHistoryModel>

    @Query("SELECT * FROM rate_history WHERE symbol = :symbol ORDER BY created_date DESC LIMIT 1")
    fun findLatestBySymbol(symbol: String): Mono<RateHistoryModel>

    @Query("SELECT DISTINCT ON (symbol) * FROM rate_history ORDER BY symbol, created_date DESC")
    fun findLatestForAllSymbols(): Flux<RateHistoryModel>

    @Query(
        "SELECT * FROM rate_history WHERE symbol = :symbol AND created_date BETWEEN :startTime AND :endTime " +
            "ORDER BY created_date ASC"
    )
    fun findBySymbolAndCreatedDateBetween(
        symbol: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): Flux<RateHistoryModel>

    @Query(
        "SELECT * FROM rate_history WHERE created_date BETWEEN :startTime AND :endTime ORDER BY symbol, created_date ASC"
    )
    fun findAllByCreatedDateBetween(startTime: LocalDateTime, endTime: LocalDateTime): Flux<RateHistoryModel>
}
