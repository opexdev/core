package co.nilin.opex.market.ports.postgres.dao

import co.nilin.opex.market.core.inout.RateSource
import co.nilin.opex.market.ports.postgres.model.CurrencyRateModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Repository
interface CurrencyRateRepository : ReactiveCrudRepository<CurrencyRateModel, Long> {

    @Query(
        """
        insert into currency_rate (base, quote, source, rate)
        values (:base, :quote, source, :rate)
        on conflict (base, quote, source)
        do update set rate = excluded.rate
        """
    )
    fun createOrUpdate(base: String, quote: String, source: RateSource, rate: BigDecimal): Mono<Void>

    @Query("select * from currency_rate where base = :base and quote = :quote and source = :source")
    fun findByBaseAndQuoteAndSource(base: String, quote: String, source: RateSource): Mono<CurrencyRateModel>

    @Query("select * from currency_rate where quote = :quote and source = :source")
    fun findAllByQuoteAndSource(quote: String, source: RateSource): Flux<CurrencyRateModel>

}