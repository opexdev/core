package co.nilin.opex.profile.ports.postgres.dao

import co.nilin.opex.profile.ports.postgres.model.entity.RevokePermissionModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RevokePermissionRepository : ReactiveCrudRepository<RevokePermissionModel, Long> {
}