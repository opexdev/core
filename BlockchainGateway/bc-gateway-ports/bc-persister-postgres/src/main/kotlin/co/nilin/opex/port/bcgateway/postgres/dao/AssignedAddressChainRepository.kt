package co.nilin.opex.port.bcgateway.postgres.dao

import co.nilin.opex.port.bcgateway.postgres.model.AssignedAddressChainModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AssignedAddressChainRepository : ReactiveCrudRepository<AssignedAddressChainModel, Long> {
    @Query("select * from assigned_address_chains where assigned_address_id = :assignedAddress")
    fun findByAssignedAddress(@Param("assignedAddress") type: Long): Flow<AssignedAddressChainModel>
}