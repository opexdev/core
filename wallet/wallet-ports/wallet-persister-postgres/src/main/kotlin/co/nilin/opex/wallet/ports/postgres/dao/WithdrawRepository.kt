package co.nilin.opex.wallet.ports.postgres.dao

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
    fun findByWallet(@Param("wallet") wallet: Long): Flow<WithdrawModel>

    @Query(
            """
        select * from withdraws wth
        join wallet wm on wm.id = wth.wallet
        where wm.owner = :owner
        """
    )
    fun findByOwner(@Param("owner") owner: Long): Flow<WithdrawModel>

    @Query(
            """
        select * from withdraws wth  
        join wallet wm on wm.id = wth.wallet    
        join wallet_owner wo on wm.owner = wo.id   
        where ( :owner is null or wo.uuid = :owner)  
            and (:withdraw_id is null or wth.id = :withdraw_id ) 
            and (:dest_transaction_ref is null or wth.dest_transaction_ref = :dest_transaction_ref) 
            and (:dest_address is null or wth.dest_address = :dest_address) 
            and (:no_status IS TRUE or wth.status in (:status)) 
            and (:currency is null or wm.currency in (:currency)) 
        order by wth.id asc
        """
    )
    fun findByCriteria(
            @Param("owner") ownerUuid: String?,
            @Param("withdraw_id") withdrawId: Long?,
            @Param("currency") currency: String?,
            @Param("dest_transaction_ref") destTxRef: String?,
            @Param("dest_address") destAddress: String?,
            @Param("no_status") noStatus: Boolean,
            @Param("status") status: List<String>?
    ): Flow<WithdrawModel>

    @Query(
            """
        select * from withdraws wth  
        join wallet wm on wm.id = wth.wallet    
        join wallet_owner wo on wm.owner = wo.id   
        where ( :owner is null or wo.uuid = :owner)  
            and (:withdraw_id is null or wth.id = :withdraw_id ) 
            and (:dest_transaction_ref is null or wth.dest_transaction_ref = :dest_transaction_ref) 
            and (:dest_address is null or wth.dest_address = :dest_address) 
            and (:no_status IS TRUE or wth.status in (:status)) 
            and (:currency is null or wm.currency in (:currency)) 
        order by wth.id asc
        offset :offset limit :size
        """
    )
    fun findByCriteria(
            @Param("owner") ownerUuid: String?,
            @Param("withdraw_id") withdrawId: Long?,
            @Param("currency") currency: String?,
            @Param("dest_transaction_ref") destTxRef: String?,
            @Param("dest_address") destAddress: String?,
            @Param("no_status") noStatus: Boolean,
            @Param("status") status: List<String>?,
            offset: Int,
            size: Int
    ): Flow<WithdrawModel>

    @Query(
            """
        select count(*) from withdraws wth  
        join wallet wm on wm.id = wth.wallet    
        join wallet_owner wo on wm.owner = wo.id   
        where ( :owner is null or wo.uuid = :owner)  
            and (:withdraw_id is null or wth.id = :withdraw_id ) 
            and (:dest_transaction_ref is null or wth.dest_transaction_ref = :dest_transaction_ref) 
            and (:dest_address is null or wth.dest_address = :dest_address) 
            and (:no_status IS TRUE or wth.status in (:status)) 
            and (:currency is null or wm.currency in (:currency)) 
        """
    )
    fun countByCriteria(
            @Param("owner") ownerUuid: String?,
            @Param("withdraw_id") withdrawId: Long?,
            @Param("currency") currency: String?,
            @Param("dest_transaction_ref") destTxRef: String?,
            @Param("dest_address") destAddress: String?,
            @Param("no_status") noStatus: Boolean,
            @Param("status") status: List<String>?
    ): Mono<Long>

    @Query("select * from withdraws where wallet = :wallet and transaction_id = :tx_id")
    fun findByWalletAndTransactionId(
            @Param("wallet") wallet: Long,
            @Param("tx_id") txId: String
    ): Mono<WithdrawModel?>

    @Query(
            """
        select * from withdraws 
        where uuid = :uuid
            and (:currency is null or currency = :currency)
            and (:startTime is null or create_date > :startTime )
            and (:endTime is null or create_date <= :endTime)
        order by create_date ASC 
        limit :limit
        offset :offset
        """
    )
    fun findWithdrawHistoryAsc(
            @Param("uuid") uuid: String,
            @Param("currency") currency: String?,
            @Param("startTime") startTime: LocalDateTime?,
            @Param("endTime") endTime: LocalDateTime?,
            @Param("limit") limit: Int,
            @Param("offset") offset: Int
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
            @Param("uuid") uuid: String,
            @Param("currency") currency: String?,
            @Param("startTime") startTime: LocalDateTime?,
            @Param("endTime") endTime: LocalDateTime?,
            @Param("limit") limit: Int,
            @Param("offset") offset: Int
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