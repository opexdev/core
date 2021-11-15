package co.nilin.opex.port.bcgateway.postgres.dao

import co.nilin.opex.port.bcgateway.postgres.model.CurrencyModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface CurrencyRepository : ReactiveCrudRepository<CurrencyModel, String> {
    fun findBySymbol(symbol: String): Mono<CurrencyModel>
}
