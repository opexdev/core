package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.core.model.DepositStatus
import co.nilin.opex.wallet.core.model.DepositType
import co.nilin.opex.wallet.core.model.WithdrawStatus
import co.nilin.opex.wallet.ports.postgres.model.DepositModel
import co.nilin.opex.wallet.ports.postgres.model.WithdrawModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.math.BigDecimal
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
            and status in (:status)
        order by  CASE WHEN :ascendingByTime=true THEN create_date END ASC,
                  CASE WHEN :ascendingByTime=false THEN create_date END DESC
        limit :limit
        offset :offset
        """
    )
    fun findDepositHistory(
        uuid: String,
        currency: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int?=0,
        offset: Int?=10000,
        ascendingByTime: Boolean?=false,
        status:List<DepositStatus>?= listOf<DepositStatus>(DepositStatus.DONE,DepositStatus.INVALID)
    ): Flow<DepositModel>


    @Query(
        """
        select * from deposits 
        where ( :owner is null or uuid = :owner)
            and (:sourceAddress is null or source_address = :sourceAddress) 
            and (:currency is null or currency =:currency) 
            and (:transactionRef is null or transaction_ref =:transactionRef)
            and (:startTime is null or create_date > :startTime )
            and (:endTime is null or create_date <= :endTime)
            and (:status is null or status in (:status)) 
        order by  CASE WHEN :ascendingByTime=true THEN create_date END ASC,
                  CASE WHEN :ascendingByTime=false THEN create_date END DESC
        offset :offset limit :limit;
        """
    )
    fun findByCriteria(
        owner: String?,
        currency: String?,
        sourceAddress: String?,
        transactionRef: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        status:List<DepositStatus>?,
        ascendingByTime: Boolean?=false,
        offset: Int?=0,
        limit: Int?=10000
    ): Flow<DepositModel>

}

