package co.nilin.opex.accountant.ports.postgres.dao

import co.nilin.opex.accountant.ports.postgres.model.CurrencyRateModel
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
        insert into currency_rate (base, quote, rate)
        values (:base, :quote, :rate)
        on conflict (base, quote)
        do update set rate = excluded.rate
        """
    )
    fun createOrUpdate(base: String, quote: String, rate: BigDecimal): Mono<Void>

    @Query("select * from currency_rate where base = :base and quote = :quote and source = :source")
    fun findByBaseAndQuote(base: String, quote: String): Mono<CurrencyRateModel>

    @Query("select * from currency_rate where quote = :quote and source = :source")
    fun findAllByQuote(quote: String): Flux<CurrencyRateModel>

}