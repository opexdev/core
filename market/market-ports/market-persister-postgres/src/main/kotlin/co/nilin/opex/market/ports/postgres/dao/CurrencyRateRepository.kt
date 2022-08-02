package co.nilin.opex.market.ports.postgres.dao

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
        insert into currency_rate (source, destination, rate)
        values (:source, :destination, :rate)
        on conflict (source, destination)
        do update set rate = excluded.rate
        """
    )
    fun createOrUpdate(source: String, destination: String, rate: BigDecimal): Mono<Void>

    @Query("select * from currency_rate where source = :source and destination = :destination")
    fun findBySourceAndDestination(source: String, destination: String): Mono<CurrencyRateModel>

    @Query("select * from currency_rate where destination = :destination")
    fun findAllByDestinationCurrency(destination: String): Flux<CurrencyRateModel>

}