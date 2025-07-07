package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.core.model.QuoteCurrency
import co.nilin.opex.wallet.ports.postgres.model.QuoteCurrencyModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface QuoteCurrencyRepository : ReactiveCrudRepository<QuoteCurrencyModel, Long> {

    fun findByCurrency(currency: String): Mono<QuoteCurrencyModel>

    @Query("SELECT * FROM quote_currency WHERE (:isActive IS NULL OR is_active = :isActive)")
    fun findAllByActive(isActive: Boolean?): Flow<QuoteCurrency>

}