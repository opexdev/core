package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.core.inout.AdminSwapResponse
import co.nilin.opex.wallet.core.model.otc.ReservedStatus
import co.nilin.opex.wallet.ports.postgres.model.ReservedTransferModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface ReservedTransferRepository : ReactiveCrudRepository<ReservedTransferModel, Long> {
    fun findByReserveNumber(reservedNumber: String): Mono<ReservedTransferModel>?


    @Query(
        """
        select * from reserved_transfer 
        where ( :owner is null or sender_uuid=:owner)
            and (:sourceSymbol is null or source_symbol =:sourceSymbol)
            and (:destSymbol is null or dest_symbol =:destSymbol)
            and (:startTime is null or reserve_date > :startTime )
            and (:endTime is null or reserve_date <= :endTime)
            and (:status is null or status=:status)
        order by  CASE WHEN :ascendingByTime=true THEN reserve_date END ASC,
                  CASE WHEN :ascendingByTime=false THEN reserve_date END DESC
        offset :offset limit :limit;
        """
    )
    fun findByCriteria(
        owner: String?,
        sourceSymbol: String?,
        destSymbol: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        ascendingByTime: Boolean? = false,
        limit: Int? = 10000,
        offset: Int? = 0,
        status: ReservedStatus?
    ): Flow<ReservedTransferModel>?

    @Query(
        """
        select count(*) from reserved_transfer 
        where ( :owner is null or sender_uuid=:owner)
            and (:sourceSymbol is null or source_symbol =:sourceSymbol)
            and (:destSymbol is null or dest_symbol =:destSymbol)
            and (:startTime is null or reserve_date > :startTime )
            and (:endTime is null or reserve_date <= :endTime)
            and (:status is null or status=:status)
        """
    )
    fun countByCriteria(
        owner: String?,
        sourceSymbol: String?,
        destSymbol: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        status: ReservedStatus?
    ): Mono<Long>

    @Query(
        """
    select 
        rt.reserve_number as reserve_number,
        rt.source_symbol as source_symbol,
        rt.dest_symbol as dest_symbol,
        rt.sender_uuid as uuid,
        split_part(wo.title, '|', 2) as owner_name,
        rt.source_amount as source_amount,
        rt.reserved_dest_amount as reserved_dest_amount,
        rt.reserve_date as reserve_date,
        rt.exp_date as exp_date,
        rt.status as status,
        rt.rate as rate
    from reserved_transfer rt
     join wallet_owner wo on wo.uuid = rt.sender_uuid
    where (:owner is null or rt.sender_uuid = :owner)
      and (:sourceSymbol is null or rt.source_symbol = :sourceSymbol)
      and (:destSymbol is null or rt.dest_symbol = :destSymbol)
      and (:startTime is null or rt.reserve_date > :startTime)
      and (:endTime is null or rt.reserve_date <= :endTime)
      and (:status is null or rt.status = :status)
    order by 
      CASE WHEN :ascendingByTime = true THEN rt.reserve_date END ASC,
      CASE WHEN :ascendingByTime = false THEN rt.reserve_date END DESC
    offset :offset 
    limit :limit;
    """
    )
    fun findByCriteriaForAdmin(
        owner: String?,
        sourceSymbol: String?,
        destSymbol: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        ascendingByTime: Boolean? = false,
        limit: Int? = 10000,
        offset: Int? = 0,
        status: ReservedStatus?
    ): Flow<AdminSwapResponse>
}