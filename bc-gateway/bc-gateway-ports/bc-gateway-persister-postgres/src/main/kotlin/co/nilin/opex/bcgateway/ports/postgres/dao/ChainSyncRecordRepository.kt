package co.nilin.opex.bcgateway.ports.postgres.dao

import co.nilin.opex.bcgateway.ports.postgres.model.ChainSyncRecordModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface ChainSyncRecordRepository : ReactiveCrudRepository<ChainSyncRecordModel, String> {

    @Query("insert into chain_sync_records values(:chain, :time, :endpointUrl, :latestBlock, :success, :error)")
    fun insert(
        chain: String,
        time: LocalDateTime,
        endpointUrl: String,
        latestBlock: Long?,
        success: Boolean,
        error: String?
    ):Mono<ChainSyncRecordModel>

    fun findByChain(chain: String): Mono<ChainSyncRecordModel>
}
