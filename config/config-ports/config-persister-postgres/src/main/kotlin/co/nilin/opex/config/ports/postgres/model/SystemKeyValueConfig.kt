package co.nilin.opex.config.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table
data class SystemKeyValueConfig(
    @Id
    val key: String,
    val value: String
)