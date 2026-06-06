package co.nilin.opex.wallet.ports.postgres.dto

data class TerminalView(
    var id: Long?,
    val uuid: String,
    val owner: String?,
    val identifier: String,
    val active: Boolean,
    val metaData: String,
    val displayOrder: Int?,
    val description: String?
)
