package co.nilin.opex.wallet.core.inout

import java.util.*

data class BankDataCommand(
    var uuid: String?,
    var owner: String,
    var identifier: String,
    var active: Boolean? = true,
    var type: TransferMethod,
    var bankSwiftCode: String
)
