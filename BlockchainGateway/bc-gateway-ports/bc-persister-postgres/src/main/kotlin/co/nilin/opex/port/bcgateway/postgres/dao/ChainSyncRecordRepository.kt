package co.nilin.opex.port.bcgateway.postgres.dao

import co.nilin.opex.port.bcgateway.postgres.model.ChainSyncRecordModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface ChainSyncRecordRepository : ReactiveCrudRepository<ChainSyncRecordModel, String> {
    fun findByChain(chain: String): Mono<ChainSyncRecordModel>
}
