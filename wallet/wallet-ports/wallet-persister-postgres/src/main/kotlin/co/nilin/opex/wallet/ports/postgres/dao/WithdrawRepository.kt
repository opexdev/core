package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.core.model.WithdrawStatus
import co.nilin.opex.wallet.ports.postgres.model.WithdrawModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface WithdrawRepository : ReactiveCrudRepository<WithdrawModel, String> {

    @Query("select * from withdraws where wallet = :wallet")
    fun findByWallet(wallet: Long): Flow<WithdrawModel>

    @Query(
        """
        select * from withdraws wth
        join wallet wm on wm.id = wth.wallet
        where wm.owner = :owner
        """
    )
    fun findByOwner(owner: Long): Flow<WithdrawModel>

    @Query(
        """
        select * from withdraws wth  
        join wallet wm on wm.id = wth.wallet    
        join wallet_owner wo on wm.owner = wo.id   
        where ( :owner is null or wo.uuid = :owner)  
            and (:withdrawId is null or wth.id = :withdrawId ) 
            and (:destTxRef is null or wth.dest_transaction_ref = :destTxRef) 
            and (:destAddress is null or wth.dest_address = :destAddress) 
            and (:noStatus IS TRUE or wth.status in (:status)) 
            and (:currency is null or wm.currency in (:currency)) 
        order by wth.id
        """
    )
    fun findByCriteria(
        owner: String?,
        withdrawId: Long?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        noStatus: Boolean,
        status: List<WithdrawStatus>?
    ): Flow<WithdrawModel>

    @Query(
        """
        select * from withdraws wth  
        join wallet wm on wm.id = wth.wallet    
        join wallet_owner wo on wm.owner = wo.id   
        where ( :owner is null or wo.uuid = :owner)  
            and (:withdrawId is null or wth.id = :withdrawId ) 
            and (:destTxRef is null or wth.dest_transaction_ref = :destTxRef) 
            and (:destAddress is null or wth.dest_address = :destAddress) 
            and (:noStatus IS TRUE or wth.status in (:status)) 
            and (:currency is null or wm.currency in (:currency)) 
        order by wth.id
        offset :offset limit :size
        """
    )
    fun findByCriteria(
        owner: String?, withdrawId: Long?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        noStatus: Boolean,
        status: List<WithdrawStatus>?,
        offset: Int,
        size: Int
    ): Flow<WithdrawModel>

    @Query(
        """
        select count(*) from withdraws wth  
        join wallet wm on wm.id = wth.wallet    
        join wallet_owner wo on wm.owner = wo.id   
        where ( :owner is null or wo.uuid = :owner)  
            and (:withdrawId is null or wth.id = :withdrawId ) 
            and (:destTxRef is null or wth.dest_transaction_ref = :destTxRef) 
            and (:destAddress is null or wth.dest_address = :destAddress) 
            and (:noStatus IS TRUE or wth.status in (:status)) 
            and (:currency is null or wm.currency in (:currency)) 
        """
    )
    fun countByCriteria(
        owner: String?,
        withdrawId: Long?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        noStatus: Boolean,
        status: List<WithdrawStatus>?
    ): Mono<Long>

    @Query("select * from withdraws where wallet = :wallet and transaction_id = :txId")
    fun findByWalletAndTransactionId(wallet: Long, txId: String): Mono<WithdrawModel?>

    @Query(
        """
        select * from withdraws 
        where uuid = :uuid
            and (:currency is null or currency = :currency)
            and (:startTime is null or create_date > :startTime )
            and (:endTime is null or create_date <= :endTime)
        order by create_date
        limit :limit
        offset :offset
        """
    )
    fun findWithdrawHistoryAsc(
        uuid: String,
        currency: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int
    ): Flow<WithdrawModel>

    @Query(
        """
        select * from withdraws 
        where uuid = :uuid
            and (:currency is null or currency = :currency)
            and (:startTime is null or create_date > :startTime )
            and (:endTime is null or create_date <= :endTime)
        order by create_date DESC 
        limit :limit
        offset :offset
        """
    )
    fun findWithdrawHistoryDesc(
        uuid: String,
        currency: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int
    ): Flow<WithdrawModel>

//    @Query(
//        """
//        select * from withdraws
//        where uuid = :uuid
//            and (:startTime is null or create_date > :startTime )
//            and (:endTime is null or  create_date <= :endTime)
//        limit :limit
//        """
//    )
//    fun findWithdrawHistory(
//        @Param("uuid") uuid: String,
//        @Param("startTime") startTime: LocalDateTime?,
//        @Param("endTime") endTime: LocalDateTime?,
//        @Param("limit") limit: Int,
//    ): Flow<WithdrawModel>

}