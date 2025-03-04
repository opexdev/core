package co.nilin.opex.matching.gateway.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("pair_setting")
data class PairSettingModel(
    @Id
    val pair: String,
    var isAvailable: Boolean = true,
    var updateDate: LocalDateTime? = null,
)