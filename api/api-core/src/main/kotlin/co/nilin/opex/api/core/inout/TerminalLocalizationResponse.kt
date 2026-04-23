package co.nilin.opex.api.core.inout

data class TerminalLocalizationResponse(
    val terminalUuid: String,
    val terminalLocalizations: List<TerminalLocalizationCommand>
)