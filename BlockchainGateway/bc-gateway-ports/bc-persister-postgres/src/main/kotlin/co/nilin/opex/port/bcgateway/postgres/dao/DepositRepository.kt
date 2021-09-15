package co.nilin.opex.port.bcgateway.postgres.dao

import co.nilin.opex.port.bcgateway.postgres.model.DepositModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface DepositRepository : ReactiveCrudRepository<DepositModel, Long> {
    fun findByChain(chain: String): Flow<DepositModel>

    @Query("select * from deposits where chain = :chain and wallet_sync_record is null")
    fun findByChainWhereNotSynced(chain: String): Flow<DepositModel>

    @Query("select * from deposits where wallet_record_id is null limit :count")
    fun findLimited(count: Long?): Flow<DepositModel>

    @Modifying
    @Query("update deposits set wallet_sync_record = :walletSyncRecord where id = :id")
    fun updateWalletSyncRecord(id: Long, walletSyncRecord: Long): Mono<Int>
}
