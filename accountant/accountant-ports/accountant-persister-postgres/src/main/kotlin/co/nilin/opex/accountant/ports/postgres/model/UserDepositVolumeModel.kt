package co.nilin.opex.accountant.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDate

@Table("user_deposit_volume")
data class UserDepositVolumeModel(
    @Id val id: Long? = null,
    val userId: String,
    val date: LocalDate,
    val totalAmount: BigDecimal,
    val quoteCurrency: String,
)
