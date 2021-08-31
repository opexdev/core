package co.nilin.opex.port.bcgateway.postgres.dao

import co.nilin.opex.port.bcgateway.postgres.model.ChainModel
import co.nilin.opex.port.bcgateway.postgres.model.ChainModelProjection
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ChainRepository : ReactiveCrudRepository<ChainModel, String> {
    @Query("SELECT * FROM chain WHERE name = :name")
    fun findByNameProjected(name: String): Flow<ChainModelProjection>
}