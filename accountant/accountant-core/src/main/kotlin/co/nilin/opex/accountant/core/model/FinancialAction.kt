package co.nilin.opex.accountant.core.model

import java.math.BigDecimal
import java.time.LocalDateTime

class FinancialAction(
    val parent: FinancialAction?,
    val eventType: String,
    val pointer: String,
    val symbol: String,
    val amount: BigDecimal,
    val sender: String,
    val senderWalletType: String,
    val receiver: String,
    val receiverWalletType: String,
    val createDate: LocalDateTime,
    val retryCount: Int = 0,
    val id: Long? = null
) {

    override fun toString(): String {
        return "FinancialAction(id=$id, parent=$parent, eventType='$eventType', pointer='$pointer', symbol='$symbol', amount=$amount, sender='$sender', senderWalletType='$senderWalletType', receiver='$receiver', receiverWalletType='$receiverWalletType', createDate=$createDate)"
    }
}

enum class FinancialActionStatus {
    CREATED, PROCESSED, ERROR
}