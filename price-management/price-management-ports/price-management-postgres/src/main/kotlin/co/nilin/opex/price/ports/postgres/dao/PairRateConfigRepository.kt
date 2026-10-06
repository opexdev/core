package co.nilin.opex.price.ports.postgres.dao

import co.nilin.opex.price.ports.postgres.model.PairRateConfigModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Repository
interface PairRateConfigRepository : ReactiveCrudRepository<PairRateConfigModel, Int> {

    @Query("SELECT * FROM pair_rate_config WHERE symbol = :symbol")
    fun findBySymbol(symbol: String): Mono<PairRateConfigModel>

    @Query("SELECT * FROM pair_rate_config WHERE is_active = true AND price_mode = 'AUTO'")
    fun findActiveAutoConfigs(): Flux<PairRateConfigModel>

    @Query(
        """
    INSERT INTO pair_rate_config (
        symbol,
        strategy,
        margin,
        is_active,
        price_mode
    )
    VALUES (
        :symbol,
        :strategy,
        :margin,
        :isActive,
        :priceMode
    )
    ON CONFLICT (symbol)
    DO UPDATE SET
        strategy = :strategy,
        margin = :margin,
        is_active = :isActive,
        price_mode = :priceMode
"""
    )
    fun upsert(
        symbol: String,
        strategy: String?,
        margin: BigDecimal?,
        isActive: Boolean,
        priceMode: String
    ): Mono<Void>
}
