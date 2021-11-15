package co.nilin.opex.bcgateway.ports.postgres.dao

import co.nilin.opex.bcgateway.ports.postgres.model.CurrencyModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface CurrencyRepository : ReactiveCrudRepository<CurrencyModel, String> {
    fun findBySymbol(symbol: String): Mono<CurrencyModel>
}
