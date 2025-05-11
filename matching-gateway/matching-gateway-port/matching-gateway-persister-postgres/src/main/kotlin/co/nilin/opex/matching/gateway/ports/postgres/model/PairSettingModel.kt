package co.nilin.opex.matching.gateway.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("pair_setting")
data class PairSettingModel(
    @Id
    val pair: String,
    var isAvailable: Boolean,
    var minOrder : BigDecimal,
    var maxOrder : BigDecimal,
    var orderTypes : String,
    var updateDate: LocalDateTime? = null,
)