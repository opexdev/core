package co.nilin.opex.port.bcgateway.postgres.dao

import co.nilin.opex.port.bcgateway.postgres.model.AssignedAddressModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface AssignedAddressRepository : ReactiveCrudRepository<AssignedAddressModel, Long> {
    @Query("select * from assigned_addresses where uuid = :uuid and addr_type_id in (:addressTypes)")
    fun findByUuidAndAddressType(
        @Param("uuid") uuid: String, @Param("addressTypes") types: List<Long>
    ): Flow<AssignedAddressModel>

    @Query("select * from assigned_addresses where address = :address and (:memo is null or memo = :memo)")
    fun findByAddressAndMemo(
        @Param("address") address: String, @Param("memo") memo: String?
    ): Mono<AssignedAddressModel>
}