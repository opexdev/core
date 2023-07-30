package co.nilin.opex.kyc.ports.postgres.model.entity

import co.nilin.opex.kyc.ports.postgres.model.base.UserStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("user_status")
data class UserStatusModel(@Id var id: Long) : UserStatus()
