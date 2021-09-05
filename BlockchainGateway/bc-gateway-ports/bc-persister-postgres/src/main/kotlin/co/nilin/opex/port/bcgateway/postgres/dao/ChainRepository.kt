package co.nilin.opex.port.bcgateway.postgres.dao

import co.nilin.opex.port.bcgateway.postgres.model.AddressTypeModel
import co.nilin.opex.port.bcgateway.postgres.model.ChainEndpointModel
import co.nilin.opex.port.bcgateway.postgres.model.ChainModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ChainRepository : ReactiveCrudRepository<ChainModel, String> {
    fun findByName(name: String): Flow<ChainModel>

    @Query(
        """
            select chain_address_types.chain_name, address_types.address_type, address_types.address_regex, address_types.memo_regex
            from chain_address_types
            join address_types
            on address_types.id = chain_address_types.addr_type_id
            where chain_name = :name
        """
    )
    fun findAddressTypesByName(name: String): Flow<AddressTypeModel>

    @Query("select * from chain_endpoints where chain_name = :name")
    fun findEndpointsByName(name: String): Flow<ChainEndpointModel>
}