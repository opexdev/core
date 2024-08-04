package co.nilin.opex.accountant.core.model

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

enum class FinancialActionCategory {
    ORDER_CREATE, ORDER_CANCEL, ORDER_FINALIZED, TRADE, FEE
}

class FinancialAction(
    val parent: FinancialAction?,
    val eventType: String,
    val pointer: String,
    val symbol: String,
    val amount: BigDecimal,
    val sender: String,
    val senderWalletType: WalletType,
    val receiver: String,
    val receiverWalletType: WalletType,
    val createDate: LocalDateTime,
    val category: FinancialActionCategory,
    val status: FinancialActionStatus = FinancialActionStatus.CREATED,
    val uuid: String = UUID.randomUUID().toString(),
    val id: Long? = null
) {

    fun isProcessed() = status == FinancialActionStatus.PROCESSED

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is FinancialAction) return false
        return if (id != null && other.id != null)
            id == other.id
        else
            uuid == other.uuid
    }

    override fun toString(): String {
        return "FinancialAction(parent=$parent, eventType='$eventType', pointer='$pointer', symbol='$symbol', amount=$amount, sender='$sender', senderWalletType='$senderWalletType', receiver='$receiver', receiverWalletType='$receiverWalletType', createDate=$createDate, category=$category, status=$status, uuid='$uuid', id=$id)"
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + (id?.hashCode() ?: 0)
        return result
    }
}

enum class FinancialActionStatus {
    CREATED,
    SENT,
    PROCESSED,
    RETRYING,
    ERROR
}