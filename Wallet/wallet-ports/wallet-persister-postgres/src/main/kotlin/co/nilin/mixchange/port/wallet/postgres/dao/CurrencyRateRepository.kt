package co.nilin.mixchange.port.wallet.postgres.dao

import co.nilin.mixchange.port.wallet.postgres.model.CurrencyModel
import co.nilin.mixchange.port.wallet.postgres.model.CurrencyRateModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface CurrencyRateRepository: ReactiveCrudRepository<CurrencyRateModel, Long> {
    @Query("select * from currency_rate where source_currency = :sourceCurrency and dest_currency = :destCurrency")
    fun findBySourceAndDest(@Param("source") sourceCurrency: String
    , @Param("dest") destCurrency: String): Mono<CurrencyRateModel?>
}