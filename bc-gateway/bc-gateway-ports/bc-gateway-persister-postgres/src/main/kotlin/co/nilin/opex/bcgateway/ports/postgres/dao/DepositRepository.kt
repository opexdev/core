package co.nilin.opex.bcgateway.ports.postgres.dao

import co.nilin.opex.bcgateway.ports.postgres.model.DepositModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface DepositRepository : ReactiveCrudRepository<DepositModel, Long> {
    @Query("select * from deposits where hash in (:hash)")
    fun findAllByHash(hash: List<String>): Flow<DepositModel>
}