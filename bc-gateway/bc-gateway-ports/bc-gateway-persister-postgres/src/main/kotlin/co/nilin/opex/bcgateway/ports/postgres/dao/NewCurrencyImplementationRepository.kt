package co.nilin.opex.bcgateway.ports.postgres.dao

import co.nilin.opex.bcgateway.ports.postgres.model.CurrencyImplementationModel
import co.nilin.opex.bcgateway.ports.postgres.model.NewCurrencyImplementationModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface NewCurrencyImplementationRepository : ReactiveCrudRepository<NewCurrencyImplementationModel, Long> {
    fun findByCurrencyImplUuid(uuid:String): Mono<NewCurrencyImplementationModel>?

}