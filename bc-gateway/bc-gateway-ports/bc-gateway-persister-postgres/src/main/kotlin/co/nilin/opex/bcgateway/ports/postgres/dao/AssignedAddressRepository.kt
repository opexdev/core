package co.nilin.opex.bcgateway.ports.postgres.dao

import co.nilin.opex.bcgateway.core.model.AddressStatus
import co.nilin.opex.bcgateway.ports.postgres.model.AssignedAddressModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface AssignedAddressRepository : ReactiveCrudRepository<AssignedAddressModel, Long> {

    @Query("select * from assigned_addresses where uuid = :uuid and addr_type_id in (:addressTypes) and (:status is null or status =:status) ")
    fun findByUuidAndAddressTypeAndStatus(
        @Param("uuid") uuid: String,
        @Param("addressTypes") types: List<Long>,
        @Param("status") status:AddressStatus?=null
    ): Flow<AssignedAddressModel>

    @Query("select * from assigned_addresses where address = :address and (memo is null or memo = '' or memo = :memo)")
    fun findByAddressAndMemo(
        @Param("address") address: String,
        @Param("memo") memo: String?
    ): Mono<AssignedAddressModel>



    @Query("select * from assigned_addresses where address = :address and (memo is null or memo = '' or memo = :memo) and (:status is null or status =:status)")
    fun findByAddressAndMemoAndStatus(
            @Param("address") address: String,
            @Param("memo") memo: String?,
            @Param("status") status:AddressStatus?=null
    ): Mono<AssignedAddressModel>

    @Query("select * from assigned_addresses where (:windowPoint is null or assigned_date > :windowPoint ) and (:now is null or exp_time< :now ) and (:status is null or status =:status) ")
    fun findPotentialExpAddress(
            @Param("windowPoint") windowPont: LocalDateTime?,
            @Param("now") now: LocalDateTime?,
            @Param("status") status:AddressStatus?=null
    ): Flow<AssignedAddressModel>?
}