package co.nilin.opex.wallet.app.dto

import co.nilin.opex.wallet.core.inout.TerminalLocalizationCommand

data class TerminalLocalizationRequest(
    val terminalLocalizations: List<TerminalLocalizationCommand>
)