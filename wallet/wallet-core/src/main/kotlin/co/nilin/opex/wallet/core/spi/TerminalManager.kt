package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.TerminalCommand
import co.nilin.opex.wallet.core.inout.TerminalLocalizationCommand

interface TerminalManager {

    suspend fun save(terminalCommand: TerminalCommand): TerminalCommand?
    suspend fun update(terminalCommand: TerminalCommand): TerminalCommand?
    suspend fun delete(uuid: String)
    suspend fun fetchTerminal(): List<TerminalCommand>?
    suspend fun fetchTerminal(uuid: String): TerminalCommand?
    suspend fun saveTerminalLocalizations(terminalUuid : String , terminalLocalizations : List<TerminalLocalizationCommand>) : List<TerminalLocalizationCommand>
    suspend fun fetchTerminalLocalizations(terminalUuid : String) : List<TerminalLocalizationCommand>
    suspend fun deleteTerminalLocalizations(id : Long)
}