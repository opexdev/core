package co.nilin.opex.market.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDate

@Table("user_trade_volume")
class UserTradeVolumeModel(
    val userId: String,
    val pair: String,
    val date: LocalDate,
    val value: BigDecimal,
    @Id val id: Long? = null
)