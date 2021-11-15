package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.CurrencyRateModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface CurrencyRateRepository : ReactiveCrudRepository<CurrencyRateModel, Long> {
    @Query("select * from currency_rate where source_currency = :sourceCurrency and dest_currency = :destCurrency")
    fun findBySourceAndDest(
        @Param("source") sourceCurrency: String, @Param("dest") destCurrency: String
    ): Mono<CurrencyRateModel?>
}
