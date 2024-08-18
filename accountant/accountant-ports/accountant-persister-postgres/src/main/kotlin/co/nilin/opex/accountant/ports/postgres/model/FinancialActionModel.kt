package co.nilin.opex.accountant.ports.postgres.model

import co.nilin.opex.accountant.core.model.FinancialActionCategory
import co.nilin.opex.accountant.core.model.FinancialActionStatus
import co.nilin.opex.accountant.core.model.WalletType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("fi_actions")
data class FinancialActionModel(
    @Id var id: Long?,
    val uuid: String,
    var parentId: Long?,
    val eventType: String,
    val pointer: String,
    val symbol: String,
    val amount: BigDecimal,
    val sender: String,
    val senderWalletType: WalletType,
    val receiver: String,
    val receiverWalletType: WalletType,
    val categoryName: FinancialActionCategory,
    val agent: String,
    val ip: String,
    val createDate: LocalDateTime,
    val status: FinancialActionStatus = FinancialActionStatus.CREATED
)


