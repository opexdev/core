package co.nilin.opex.profile.ports.postgres.model.entity

import co.nilin.opex.profile.ports.postgres.model.base.RevokePermission
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("revoke_permission")
data class RevokePermissionModel ( @Id var id: Long):RevokePermission()
