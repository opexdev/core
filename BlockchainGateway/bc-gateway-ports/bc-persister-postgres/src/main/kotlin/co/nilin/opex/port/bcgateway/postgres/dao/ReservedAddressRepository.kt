package co.nilin.opex.port.bcgateway.postgres.dao

import co.nilin.opex.port.bcgateway.postgres.model.ReservedAddressModel
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface ReservedAddressRepository : ReactiveCrudRepository<ReservedAddressModel, Long> {

    @Query("select * from reserved_addresses where address_type = :addressType order by id DESC")
    fun peekFirstAdded(addressType: Long): Flux<ReservedAddressModel>

    @Modifying
    @Query("delete from reserved_addresses where address = :address and (memo is null or memo = :memo)")
    fun remove(address: String, memo: String?): Mono<Int>
}
