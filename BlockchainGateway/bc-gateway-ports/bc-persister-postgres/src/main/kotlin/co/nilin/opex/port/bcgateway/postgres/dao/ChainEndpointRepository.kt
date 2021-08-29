package co.nilin.opex.port.bcgateway.postgres.dao

import co.nilin.opex.port.bcgateway.postgres.model.ChainEndpointModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface ChainEndpointRepository : ReactiveCrudRepository<ChainEndpointModel, Long> {

}