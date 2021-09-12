package co.nilin.opex.port.bcgateway.postgres.dao

import co.nilin.opex.port.bcgateway.postgres.model.DepositModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface DepositRepository : ReactiveCrudRepository<DepositModel, Long> {
    fun findByChain(chain: String): Flow<DepositModel>
}