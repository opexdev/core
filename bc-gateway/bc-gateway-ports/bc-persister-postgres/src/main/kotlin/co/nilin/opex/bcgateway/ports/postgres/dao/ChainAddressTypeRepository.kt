package co.nilin.opex.bcgateway.ports.postgres.dao

import co.nilin.opex.bcgateway.ports.postgres.model.ChainAddressTypeModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ChainAddressTypeRepository : ReactiveCrudRepository<ChainAddressTypeModel, Long>