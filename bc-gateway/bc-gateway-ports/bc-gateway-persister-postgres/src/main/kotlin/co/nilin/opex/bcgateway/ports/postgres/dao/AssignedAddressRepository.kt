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
        @Param("status") status: AddressStatus? = null
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
        @Param("status") status: AddressStatus? = null
    ): Mono<AssignedAddressModel>

    @Query("select * from assigned_addresses where (:windowPoint is null or assigned_date > :windowPoint ) and (:now is null or exp_time< :now ) and (:status is null or status =:status) ")
    fun findPotentialExpAddress(
        @Param("windowPoint") windowPont: LocalDateTime?,
        @Param("now") now: LocalDateTime?,
        @Param("status") status: AddressStatus? = null
    ): Flow<AssignedAddressModel>?

    @Query("select * from assigned_addresses where address = :address and (memo is null or memo = '' or memo = :memo) and assigned_date <= :at and (exp_time is null or exp_time > :at) order by assigned_date desc limit 1")
    fun findHolderAt(
        @Param("address") address: String,
        @Param("memo") memo: String?,
        @Param("at") at: LocalDateTime
    ): Mono<AssignedAddressModel>

    @Query(
        """
    SELECT * FROM assigned_addresses a
    WHERE (:uuid IS NULL OR a.uuid = :uuid)
      AND (:address IS NULL OR a.address = :address)
      AND (:memo IS NULL OR a.memo = :memo)
      AND (:status IS NULL OR a.status = :status)
      AND (:addressTypeId IS NULL OR a.addr_type_id = :addressTypeId)
      AND (:chain IS NULL OR a.addr_type_id IN
           (SELECT addr_type_id FROM chain_address_types WHERE chain_name = :chain))
      AND (:assignedFrom IS NULL OR a.assigned_date >= :assignedFrom)
      AND (:assignedTo IS NULL OR a.assigned_date <= :assignedTo)
    ORDER BY a.assigned_date DESC, a.id DESC
    LIMIT :limit OFFSET :offset
"""
    )
    fun searchAssignments(
        @Param("uuid") uuid: String?,
        @Param("address") address: String?,
        @Param("memo") memo: String?,
        @Param("status") status: AddressStatus?,
        @Param("addressTypeId") addressTypeId: Long?,
        @Param("chain") chain: String?,
        @Param("assignedFrom") assignedFrom: LocalDateTime?,
        @Param("assignedTo") assignedTo: LocalDateTime?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int
    ): Flow<AssignedAddressModel>

    @Query(
        """
    SELECT count(*) FROM assigned_addresses a
    WHERE (:uuid IS NULL OR a.uuid = :uuid)
      AND (:address IS NULL OR a.address = :address)
      AND (:memo IS NULL OR a.memo = :memo)
      AND (:status IS NULL OR a.status = :status)
      AND (:addressTypeId IS NULL OR a.addr_type_id = :addressTypeId)
      AND (:chain IS NULL OR a.addr_type_id IN
           (SELECT addr_type_id FROM chain_address_types WHERE chain_name = :chain))
      AND (:assignedFrom IS NULL OR a.assigned_date >= :assignedFrom)
      AND (:assignedTo IS NULL OR a.assigned_date <= :assignedTo)
"""
    )
    fun countAssignments(
        @Param("uuid") uuid: String?,
        @Param("address") address: String?,
        @Param("memo") memo: String?,
        @Param("status") status: AddressStatus?,
        @Param("addressTypeId") addressTypeId: Long?,
        @Param("chain") chain: String?,
        @Param("assignedFrom") assignedFrom: LocalDateTime?,
        @Param("assignedTo") assignedTo: LocalDateTime?
    ): Mono<Long>
}