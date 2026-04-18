package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.common.data.UserLanguage
import co.nilin.opex.common.utils.LanguageUtils.getDefaultUserLanguage
import co.nilin.opex.common.utils.LanguageUtils.getUserLanguage
import co.nilin.opex.wallet.core.inout.TerminalCommand
import co.nilin.opex.wallet.core.inout.TerminalLocalizationCommand
import co.nilin.opex.wallet.core.spi.TerminalManager
import co.nilin.opex.wallet.ports.postgres.dao.TerminalLocalizationsRepository
import co.nilin.opex.wallet.ports.postgres.dao.TerminalRepository
import co.nilin.opex.wallet.ports.postgres.model.TerminalLocalizationModel
import co.nilin.opex.wallet.ports.postgres.model.TerminalModel
import co.nilin.opex.wallet.ports.postgres.util.toCommand
import co.nilin.opex.wallet.ports.postgres.util.toModel
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Component
class TerminalManagerImpl(
    private val terminalRepository: TerminalRepository,
    private val terminalLocalizationsRepository: TerminalLocalizationsRepository,
    private val transactionalOperator: TransactionalOperator

) : TerminalManager {
    override suspend fun save(terminalCommand: TerminalCommand): TerminalCommand? {

        terminalRepository.findByIdentifier(terminalCommand.identifier)
            ?.awaitSingleOrNull()
            ?.let { throw OpexError.TerminalIsExist.exception() }

        val terminal = terminalRepository
            .save(terminalCommand.toModel())
            .awaitSingleOrNull()
            ?: return null

        val terminalId = terminal.id ?: return null

        terminalCommand.description
            ?.takeIf { it.isNotBlank() }
            ?.let { description ->
                terminalLocalizationsRepository.save(
                    TerminalLocalizationModel(
                        terminalId = terminalId,
                        description = description,
                        language = getDefaultUserLanguage()
                    )
                ).awaitSingleOrNull()
            }

        return terminalCommand.apply { uuid = terminal.uuid }
    }

    override suspend fun update(terminalCommand: TerminalCommand): TerminalCommand? {
        loadTerminal(terminalCommand.uuid!!)?.let {
            val terminal = terminalRepository
                .save(terminalCommand.toModel().apply { id = it.id })
                .awaitSingleOrNull()
                ?: return null
            return terminalRepository.findByUuid(terminal.uuid!!, getDefaultUserLanguage())
                ?.awaitSingleOrNull()
                ?.toCommand()
        } ?: throw OpexError.TerminalNotFound.exception()
    }

    override suspend fun delete(uuid: String) {
        loadTerminal(uuid)?.let {
            terminalRepository.deleteById(it.id!!).awaitSingleOrNull()
        } ?: throw OpexError.TerminalNotFound.exception()
    }

    override suspend fun fetchTerminal(): List<TerminalCommand>? {
        return terminalRepository.findAllByOrderByDisplayOrder(
            UserLanguage.safeValueOf(getUserLanguage().awaitSingleOrNull()).toString()
        )
            .map { it.toCommand() }.collectList().awaitSingleOrNull()
    }

    override suspend fun fetchTerminal(uuid: String): TerminalCommand? {
        return terminalRepository.findByUuid(
            uuid,
            UserLanguage.safeValueOf(getUserLanguage().awaitSingleOrNull()).toString()
        )?.awaitSingleOrNull()
            ?.toCommand()
    }

    override suspend fun saveTerminalLocalizations(
        terminalUuid: String,
        terminalLocalizations: List<TerminalLocalizationCommand>
    ): List<TerminalLocalizationCommand> {

        return transactionalOperator.executeAndAwait {

            val terminal = loadTerminal(terminalUuid)
                ?: throw OpexError.TerminalNotFound.exception()

            terminalLocalizations.forEach { t ->
                terminalLocalizationsRepository.save(
                    TerminalLocalizationModel(
                        terminalId = terminal.id!!,
                        description = t.description,
                        language = UserLanguage.safeValueOf(t.language).toString()
                    )
                ).awaitSingle()
            }

            terminalLocalizationsRepository.findByTerminalId(terminal.id!!)
                .map { it.toCommand() }
                .collectList()
                .awaitSingleOrNull()
                ?: emptyList()
        } ?: emptyList()
    }

    override suspend fun fetchTerminalLocalizations(terminalUuid: String): List<TerminalLocalizationCommand> {
        val terminal = loadTerminal(terminalUuid)
            ?: throw OpexError.TerminalNotFound.exception()
        return terminalLocalizationsRepository.findByTerminalId(terminal.id!!)
            .map { it.toCommand() }
            .collectList()
            .awaitSingleOrNull()
            ?: emptyList()
    }

    override suspend fun deleteTerminalLocalizations(id: Long) {
        terminalLocalizationsRepository.deleteById(id).awaitSingleOrNull()
    }

    private suspend fun loadTerminal(uuid: String): TerminalModel? {
        return terminalRepository.findByUuid(
            uuid,
            UserLanguage.safeValueOf(getUserLanguage().awaitSingleOrNull()).toString()
        )?.awaitSingleOrNull()
            ?.toModel()

    }
}