package co.nilin.opex.port.bcgateway.postgres.dao

import co.nilin.opex.port.bcgateway.postgres.model.ChainSyncDepositModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ChainSyncDepositRepository : ReactiveCrudRepository<ChainSyncDepositModel, Long> {
    fun findByChain(chain: String): Flow<ChainSyncDepositModel>
}
