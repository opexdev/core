package co.nilin.opex.matching.gateway.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("pair_setting")
data class PairSettingModel(
    @Id
    val pair: String,
    @Column("IS_AVAILABLE")
    var isAvailable: Boolean = true,
    @Column("update_date")
    var updateDate: LocalDateTime? = null,
)