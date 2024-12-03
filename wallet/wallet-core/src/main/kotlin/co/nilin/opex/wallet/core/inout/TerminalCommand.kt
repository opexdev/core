package co.nilin.opex.wallet.core.inout

data class TerminalCommand(
    var uuid: String?,
    var owner: String,
    var identifier: String,
    var active: Boolean? = true,
    var type: TransferMethod,
    var metaData: String,
    var description : String?
)
