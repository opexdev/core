package co.nilin.opex.accountant.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDate

@Table("user_withdraw_volume")
data class UserWithdrawVolumeModel(
    @Id val id: Long? = null,
    val userId: String,
    val date: LocalDate,
    @Column("value_usdt")
    val valueUSDT: BigDecimal,
    @Column("value_irt")
    val valueIRT: BigDecimal,
)
