package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.core.model.otc.StaleRate
import co.nilin.opex.wallet.ports.postgres.model.RateModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface RatesRepository : ReactiveCrudRepository<RateModel, Long> {

    fun findBySourceSymbolAndDestinationSymbol(sourceSymbol: String, destSymbol: String): Mono<RateModel?>?

    fun deleteBySourceSymbolAndDestinationSymbol(sourceSymbol: String, destSymbol: String): Mono<Void>?


    @Query(
        """
    SELECT source_symbol, dest_symbol, last_update_date
    FROM rate
    WHERE last_update_date IS NOT NULL
      AND last_update_date < :updatedBefore
    ORDER BY last_update_date
"""
    )
    fun findStaleRates(@Param("updatedBefore") updatedBefore: LocalDateTime): Flux<StaleRate>
}