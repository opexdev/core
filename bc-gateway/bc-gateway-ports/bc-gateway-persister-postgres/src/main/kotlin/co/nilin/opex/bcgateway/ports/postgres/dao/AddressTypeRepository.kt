package co.nilin.opex.bcgateway.ports.postgres.dao

import co.nilin.opex.bcgateway.ports.postgres.model.AddressTypeModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface AddressTypeRepository : ReactiveCrudRepository<AddressTypeModel, Long> {

    fun findByType(type: String): Mono<AddressTypeModel>

}