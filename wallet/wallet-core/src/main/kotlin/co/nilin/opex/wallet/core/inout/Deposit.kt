package co.nilin.opex.wallet.core.inout

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class Deposit(
        var ownerUuid: String,
        var depositUuid: String,
        var currency: String,
        var amount: BigDecimal,
        var acceptedFee: BigDecimal?=null,
        var appliedFee: BigDecimal?=null,
        var sourceSymbol: String?=null,
        var network: String?=null,
        var sourceAddress: String?=null,
        var transactionRef: String?=null,
        var note: String?=null,
        var status: String?=null,
        var depositType:String?=null,
        var createDate: Date?=Date(),
)



data class Deposits(var deposits: List<Deposit>)