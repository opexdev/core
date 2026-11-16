package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.TerminalCommand
import co.nilin.opex.wallet.core.inout.TerminalLocalizationCommand

interface TerminalManager {

    suspend fun save(terminalCommand: TerminalCommand, applicator: String? = null): TerminalCommand?
    suspend fun update(terminalCommand: TerminalCommand, applicator: String? = null): TerminalCommand?
    suspend fun delete(uuid: String, applicator: String? = null)
    suspend fun fetchTerminal(): List<TerminalCommand>?
    suspend fun fetchTerminal(uuid: String): TerminalCommand?
    suspend fun saveTerminalLocalizations(terminalUuid: String, terminalLocalizations: List<TerminalLocalizationCommand>, applicator: String? = null): List<TerminalLocalizationCommand>
    suspend fun fetchTerminalLocalizations(terminalUuid : String) : List<TerminalLocalizationCommand>
    suspend fun deleteTerminalLocalizations(id: Long, applicator: String? = null)
}
