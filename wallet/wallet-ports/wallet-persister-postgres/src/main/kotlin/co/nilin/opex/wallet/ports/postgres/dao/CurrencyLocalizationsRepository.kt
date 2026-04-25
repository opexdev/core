package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.CurrencyLocalizationModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface CurrencyLocalizationsRepository : ReactiveCrudRepository<CurrencyLocalizationModel, Long> {
    suspend fun findByCurrency(currency: String): Flux<CurrencyLocalizationModel>
}