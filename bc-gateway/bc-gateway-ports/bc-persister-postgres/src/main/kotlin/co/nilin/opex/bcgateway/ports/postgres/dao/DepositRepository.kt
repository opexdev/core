package co.nilin.opex.bcgateway.ports.postgres.dao

import co.nilin.opex.bcgateway.ports.postgres.model.DepositModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Repository
interface DepositRepository : ReactiveCrudRepository<DepositModel, Long> {

    fun findByChain(chain: String): Flow<DepositModel>

    @Query("select * from deposits where hash in (:hash)")
    fun findAllByHash(hash: List<String>): Flow<DepositModel>

    @Query("select * from deposits where chain = :chain and wallet_record_id is null")
    fun findByChainWhereNotSynced(chain: String): Flow<DepositModel>

    @Query("select * from deposits where wallet_record_id is null limit :count")
    fun findLimited(count: Long?): Flow<DepositModel>

    @Modifying
    @Query("update deposits set wallet_record_id = :walletRecordId where id = :id")
    fun updateWalletSyncRecord(id: Long, walletRecordId: Long): Mono<Int>

    @Modifying
    @Query("update deposits set wallet_record_id = :walletRecordId where id in (:ids)")
    fun updateWalletSyncRecords(ids: List<Long>, walletRecordId: Long): Mono<Int>

    @Modifying
    @Query(
        """
        insert into deposits(hash, chain, token, token_address, amount, depositor, depositor_memo) 
        values (:hash, :chain, :isToken, :tokenAddress, :amount, :depositor, :memo)
        on CONFLICT (hash)
        do nothing
        """
    )
    fun save(
        @Param("hash")
        hash: String,
        @Param("chain")
        chain: String,
        @Param("isToken")
        isToken: Boolean,
        @Param("tokenAddress")
        tokenAddress: String?,
        @Param("amount")
        amount: BigDecimal,
        @Param("depositor")
        depositor: String,
        @Param("memo")
        memo: String?
    ): Mono<DepositModel>

    @Modifying
    @Query("delete from deposits where id in (:ids)")
    fun deleteSyncedDeposits(ids: List<Long>): Mono<Int>

}
