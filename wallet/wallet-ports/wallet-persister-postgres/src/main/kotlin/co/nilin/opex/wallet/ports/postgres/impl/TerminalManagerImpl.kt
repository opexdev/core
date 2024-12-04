package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.inout.TerminalCommand
import co.nilin.opex.wallet.core.spi.TerminalManager
import co.nilin.opex.wallet.ports.postgres.dao.TerminalRepository
import co.nilin.opex.wallet.ports.postgres.model.TerminalModel
import co.nilin.opex.wallet.ports.postgres.util.toDto
import co.nilin.opex.wallet.ports.postgres.util.toModel
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component

@Component
class TerminalManagerImpl(private val terminalRepository: TerminalRepository) : TerminalManager {
    override suspend fun save(terminalCommand: TerminalCommand): TerminalCommand? {
        terminalRepository.findByIdentifier(terminalCommand.identifier)?.awaitSingleOrNull()
            ?.let { throw OpexError.TerminalIsExist.exception() }
        return _save(terminalCommand.toModel())?.toDto()
    }

    override suspend fun update(terminalCommand: TerminalCommand): TerminalCommand? {
        loadTerminal(terminalCommand.uuid!!)?.let {
            return _save(terminalCommand.toModel().apply { id = it.id })?.toDto()
        } ?: throw OpexError.TerminalIsExist.exception()
    }

    override suspend fun delete(uuid: String) {
        loadTerminal(uuid)?.let {
            terminalRepository.deleteById(it.id!!)?.awaitSingleOrNull()
        } ?: throw OpexError.TerminalNotFound.exception()
    }

    override suspend fun fetchTerminal(): List<TerminalCommand>? {
        return terminalRepository.findAll().map { it.toDto() }?.collectList()?.awaitSingleOrNull()
    }

    override suspend fun fetchTerminal(uuid: String): TerminalCommand? {
        return loadTerminal(uuid!!)?.let {
            it.toDto()
        } ?: throw OpexError.TerminalIsExist.exception()
    }


    private suspend fun _save(terminalModel: TerminalModel): TerminalModel? {
        return terminalRepository.save(terminalModel)?.awaitSingleOrNull()

    }

    private suspend fun loadTerminal(uuid: String): TerminalModel? {
        return terminalRepository.findByUuid(uuid)?.awaitSingleOrNull()

    }
}