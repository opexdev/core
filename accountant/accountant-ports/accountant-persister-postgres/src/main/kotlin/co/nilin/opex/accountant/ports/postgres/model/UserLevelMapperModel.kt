package co.nilin.opex.accountant.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("user_level_mapper")
data class UserLevelMapperModel(
        @Id val id: Long?,
        val uuid: String,
        val userLevel: String
)