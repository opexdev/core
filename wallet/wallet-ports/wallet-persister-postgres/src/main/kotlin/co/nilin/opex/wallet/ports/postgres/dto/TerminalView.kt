package co.nilin.opex.wallet.ports.postgres.dto

import co.nilin.opex.wallet.core.inout.TransferMethod

data class TerminalView(
    var id: Long?,
    val uuid: String,
    val owner: String?,
    val identifier: String,
    val active: Boolean,
    val type: TransferMethod,
    val metaData: String,
    val displayOrder: Int?,
    val description: String?
)
