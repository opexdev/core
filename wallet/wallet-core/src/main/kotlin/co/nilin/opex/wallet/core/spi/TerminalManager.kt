package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.TerminalCommand

interface TerminalManager {

    suspend fun save(terminalCommand: TerminalCommand): TerminalCommand?
    suspend fun update(terminalCommand: TerminalCommand): TerminalCommand?
    suspend fun delete(uuid: String)
    suspend fun fetchTerminal(): List<TerminalCommand>?
    suspend fun fetchTerminal(uuid: String): TerminalCommand?
}