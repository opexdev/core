package co.nilin.opex.wallet.ports.kafka.listener.model

import co.nilin.opex.wallet.core.model.WalletType
import java.math.BigDecimal
import java.time.LocalDateTime

class FinancialActionEvent {

    lateinit var uuid: String
    lateinit var symbol: String
    lateinit var amount: BigDecimal
    lateinit var sender: String
    lateinit var senderWalletType: WalletType
    lateinit var receiver: String
    lateinit var receiverWalletType: WalletType
    lateinit var createDate: LocalDateTime
    var transferRef: String? = null
    lateinit var transferCategory: String
    var additionalData: Map<String, Any>? = null
    lateinit var description: String

    constructor() {

    }

    constructor(
        uuid: String,
        symbol: String,
        amount: BigDecimal,
        sender: String,
        senderWalletType: WalletType,
        receiver: String,
        receiverWalletType: WalletType,
        createDate: LocalDateTime,
        transferRef: String,
        description: String,
        transferCategory: String,
        additionalData: Map<String, Any>?
    ) {
        this.uuid = uuid
        this.symbol = symbol
        this.amount = amount
        this.sender = sender
        this.senderWalletType = senderWalletType
        this.receiver = receiver
        this.receiverWalletType = receiverWalletType
        this.createDate = createDate
        this.transferRef = transferRef
        this.description = description
        this.transferCategory = transferCategory
        this.additionalData = additionalData
    }
}