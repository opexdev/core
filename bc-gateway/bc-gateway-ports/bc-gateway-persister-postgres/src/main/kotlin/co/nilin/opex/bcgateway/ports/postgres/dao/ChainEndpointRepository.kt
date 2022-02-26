package co.nilin.opex.bcgateway.ports.postgres.dao

import co.nilin.opex.bcgateway.ports.postgres.model.ChainEndpointModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface ChainEndpointRepository : ReactiveCrudRepository<ChainEndpointModel, Long> {

    fun deleteByChainNameAndUrl(chainName: String, url: String): Mono<Void>

}