package co.nilin.opex.api.core.inout

data class TerminalLocalizationCommand(
    var id : Long?,
    var description: String?=null,
    var owner: String?=null,
    var language: String,
)
