package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.DepositModel
import co.nilin.opex.wallet.ports.postgres.model.WithdrawModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface DepositRepository : ReactiveCrudRepository<DepositModel, Long> {

    @Query(
        """
        select * from deposits 
        where uuid = :uuid
            and (:currency is null or currency = :currency)
            and (:startTime is null or create_date > :startTime )
            and (:endTime is null or create_date <= :endTime)
        order by create_date ASC 
        limit :limit
        offset :offset
        """
    )
    fun findDepositHistoryAsc(
        uuid: String,
        currency: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int
    ): Flow<DepositModel>

    @Query(
        """
        select * from deposits 
        where uuid = :uuid
            and (:currency is null or currency = :currency)
            and (:startTime is null or create_date > :startTime )
            and (:endTime is null or create_date <= :endTime)
        order by create_date DESC 
        limit :limit
        offset :offset
        """
    )
    fun findDepositHistoryDesc(
        uuid: String,
        currency: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int
    ): Flow<DepositModel>
}