package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.core.model.otc.ReservedStatus
import co.nilin.opex.wallet.ports.postgres.model.DepositModel
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
        select * from deposits 
        where ( :owner is null or uuid = :owner)
            and (:currency is null or currency in (:currency)) 
            and (:startTime is null or create_date > :startTime )
            and (:endTime is null or create_date <= :endTime)
            ans (:status is null or status=:status)
        order  order by create_date (CASE WHEN :ascendingByTime = true THEN ASC ELSE DESC END)
        offset :offset limit :size;
        """
    )
    fun findByCriteria(
        owner: String?,
        currency: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        ascendingByTime: Boolean? = false,
        limit: Int? = 10000,
        offset: Int? = 0,
        status: ReservedStatus?
    ): Flow<ReservedTransferModel>?
}