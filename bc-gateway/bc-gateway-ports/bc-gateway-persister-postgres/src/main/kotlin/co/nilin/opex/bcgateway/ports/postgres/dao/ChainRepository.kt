package co.nilin.opex.bcgateway.ports.postgres.dao

import co.nilin.opex.bcgateway.ports.postgres.model.AddressTypeModel
import co.nilin.opex.bcgateway.ports.postgres.model.ChainModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface ChainRepository : ReactiveCrudRepository<ChainModel, String> {

    @Query("insert into chains (name, transaction_scanner_url, address_scanner_url, applicator) values (:name, :transactionScannerUrl, :addressScannerUrl, :applicator) on conflict do nothing")
    fun insert(
        name: String,
        transactionScannerUrl: String?,
        addressScannerUrl: String?,
        applicator: String
    ): Mono<Void>

    fun findByName(name: String): Mono<ChainModel>?

    @Query(
        """
            select distinct address_types.id, chain_address_types.chain_name, address_types.address_type, address_types.address_regex, address_types.memo_regex, address_types.applicator
            from chain_address_types
            join address_types
            on address_types.id = chain_address_types.addr_type_id
            where chain_name = :name
        """
    )
    fun findAddressTypesByName(name: String): Flow<AddressTypeModel>
}
