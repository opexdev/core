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
interface WithdrawRepository : ReactiveCrudRepository<WithdrawModel, Long> {

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
            and (:destTxRef is null or wth.dest_transaction_ref = :destTxRef) 
            and (:destAddress is null or wth.dest_address = :destAddress) 
            and (:currency is null or wm.currency in (:currency)) 
            and (:startTime is null or create_date > :startTime )
            and (:endTime is null or create_date <= :endTime)
        order by create_date (CASE WHEN :ascendingByTime = true THEN ASC ELSE DESC END)
        offset :offset limit :size;
        """
    )
    fun findByCriteria(
        owner: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        ascendingByTime: Boolean?=false,
        offset: Int?=0,
        size: Int?=10000
    ): Flow<WithdrawModel>

    @Query(
        """
        select * from withdraws wth  
        join wallet wm on wm.id = wth.wallet    
        join wallet_owner wo on wm.owner = wo.id   
        where ( :owner is null or wo.uuid = :owner)
            and (:destTxRef is null or wth.dest_transaction_ref = :destTxRef) 
            and (:destAddress is null or wth.dest_address = :destAddress) 
            and (:currency is null or wm.currency in (:currency)) 
            and wth.status in (:status)
            and (:startTime is null or create_date > :startTime )
            and (:endTime is null or create_date <= :endTime)
        order  order by create_date (CASE WHEN :ascendingByTime = true THEN ASC ELSE DESC END)
        offset :offset limit :size;
        """
    )
    fun findByCriteria(
        owner: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        status: List<WithdrawStatus>?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        ascendingByTime: Boolean?=false,
        offset: Int?=0,
        size: Int?=10000
    ): Flow<WithdrawModel>

    @Query(
        """
        select * from withdraws wth  
        join wallet wm on wm.id = wth.wallet    
        join wallet_owner wo on wm.owner = wo.id   
        where ( :owner is null or wo.uuid = :owner)  
            and (:destTxRef is null or wth.dest_transaction_ref = :destTxRef) 
            and (:destAddress is null or wth.dest_address = :destAddress) 
            and (:currency is null or wm.currency in (:currency))
        order by wth.id
        offset :offset limit :size
        """
    )
    fun findByCriteria(
        owner: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        offset: Int?=0,
        size: Int?=10000
    ): Flow<WithdrawModel>

    @Query(
        """
        select * from withdraws wth  
        join wallet wm on wm.id = wth.wallet    
        join wallet_owner wo on wm.owner = wo.id   
        where ( :owner is null or wo.uuid = :owner)  
            and (:destTxRef is null or wth.dest_transaction_ref = :destTxRef) 
            and (:destAddress is null or wth.dest_address = :destAddress) 
            and (:currency is null or wm.currency in (:currency)) 
            and wth.status in (:status)
        order by wth.id
        offset :offset limit :size
        """
    )
    fun findByCriteria(
        owner: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        status: List<WithdrawStatus>,
        offset: Int?=0,
        size: Int?=10000
    ): Flow<WithdrawModel>

    @Query(
        """
        select count(*) from withdraws wth  
        join wallet wm on wm.id = wth.wallet    
        join wallet_owner wo on wm.owner = wo.id   
        where ( :owner is null or wo.uuid = :owner)
            and (:destTxRef is null or wth.dest_transaction_ref = :destTxRef) 
            and (:destAddress is null or wth.dest_address = :destAddress) 
            and (:currency is null or wm.currency in (:currency))
        """
    )
    fun countByCriteria(
        owner: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
    ): Mono<Long>

    @Query(
        """
        select count(*) from withdraws wth  
        join wallet wm on wm.id = wth.wallet    
        join wallet_owner wo on wm.owner = wo.id   
        where ( :owner is null or wo.uuid = :owner)
            and (:destTxRef is null or wth.dest_transaction_ref = :destTxRef) 
            and (:destAddress is null or wth.dest_address = :destAddress) 
            and (:currency is null or wm.currency in (:currency)) 
            and wth.status in (:status)
        """
    )
    fun countByCriteria(
        owner: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        status: List<WithdrawStatus>
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
        order by create_date  (CASE WHEN :ascendingByTime = true THEN ASC ELSE DESC END)
        limit :limit
        offset :offset
        """
    )
    fun findWithdrawHistory(
        uuid: String,
        currency: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        ascendingByTime: Boolean,
        limit: Int?=0,
        offset: Int?=10000
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