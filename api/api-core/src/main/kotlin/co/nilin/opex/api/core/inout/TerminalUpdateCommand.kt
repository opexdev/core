package co.nilin.opex.api.core.inout

data class TerminalUpdateCommand(
    var uuid: String?,
    var identifier: String,
    var active: Boolean? = true,
    var metaData: String,
    var displayOrder: Int? = null,
    )
