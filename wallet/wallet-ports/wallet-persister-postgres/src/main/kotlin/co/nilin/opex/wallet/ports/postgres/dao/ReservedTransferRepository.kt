package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.core.model.WalletType
import co.nilin.opex.wallet.core.model.otc.ReservedStatus
import co.nilin.opex.wallet.ports.postgres.model.DepositModel
import co.nilin.opex.wallet.ports.postgres.model.ReservedTransferModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

@Repository
interface ReservedTransferRepository : ReactiveCrudRepository<ReservedTransferModel, Long> {
    fun findByReserveNumber(reservedNumber: String): Mono<ReservedTransferModel>?




    @Query(
        """
        select * from deposits 
        where ( :owner is null or uuid = :owner)
            and (:sourceSymbol is null or source_symbol =:sourceSymbol)
            and (:destSymbol is null or dest_symbol =:destSymbol)
            and (:startTime is null or create_date > :startTime )
            and (:endTime is null or create_date <= :endTime)
            and (:status is null or status=:status)
        order by  CASE WHEN :ascendingByTime=true THEN create_date END ASC,
                  CASE WHEN :ascendingByTime=false THEN create_date END DESC
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
}