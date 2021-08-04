package co.nilin.opex.accountant.core.model

import java.math.BigDecimal
import java.time.LocalDateTime

class FinancialAction(
    val id: Long? = null,
    val parent: FinancialAction?,
    val eventType: String,
    val pointer: String,
    val symbol: String,
    val amount: BigDecimal,
    val sender: String,
    val senderWalletType: String,
    val receiver: String,
    val receiverWalletType: String,
    val createDate: LocalDateTime
) {
    constructor(
        parent: FinancialAction?,
        eventType: String,
        pointer: String,
        symbol: String,
        amount: BigDecimal,
        sender: String,
        senderWalletType: String,
        receiver: String,
        receiverWalletType: String,
        createDate: LocalDateTime
    ) : this(null, parent, eventType, pointer, symbol, amount, sender, senderWalletType, receiver, receiverWalletType, createDate)

    override fun toString(): String {
        return "FinancialAction(id=$id, parent=$parent, eventType='$eventType', pointer='$pointer', symbol='$symbol', amount=$amount, sender='$sender', senderWalletType='$senderWalletType', receiver='$receiver', receiverWalletType='$receiverWalletType', createDate=$createDate)"
    }


}

enum class FinancialActionStatus {
    CREATED, PROCESSED, ERROR
}