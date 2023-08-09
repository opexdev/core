package co.nilin.opex.wallet.ports.kafka.listener.model

import java.math.BigDecimal
import java.time.LocalDateTime

class FinancialActionEvent {

    lateinit var uuid: String
    lateinit var symbol: String
    lateinit var amount: BigDecimal
    lateinit var sender: String
    lateinit var senderWalletType: String
    lateinit var receiver: String
    lateinit var receiverWalletType: String
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
        senderWalletType: String,
        receiver: String,
        receiverWalletType: String,
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