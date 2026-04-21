package co.nilin.opex.wallet.app.dto

import co.nilin.opex.wallet.core.inout.TerminalLocalizationCommand

data class TerminalLocalizationResponse(
    val terminalUuid: String,
    val terminalLocalizations: List<TerminalLocalizationCommand>
)