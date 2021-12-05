package co.nilin.opex.bcgateway.ports.postgres.dao

import co.nilin.opex.bcgateway.ports.postgres.model.ChainSyncRetryModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface ChainSyncRetryRepository : ReactiveCrudRepository<ChainSyncRetryModel, Long> {

    @Query("select * from chain_sync_retry where chain = :chain and block = :block")
    fun findByChainAndBlock(chain: String, block: Long): Mono<ChainSyncRetryModel?>

}