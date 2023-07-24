package co.nilin.opex.profile.ports.postgres.model.entity

import co.nilin.opex.profile.ports.postgres.model.base.Limitations
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("revoke_permission")
data class PermissionModel (@Id var id: Long):Limitations()
