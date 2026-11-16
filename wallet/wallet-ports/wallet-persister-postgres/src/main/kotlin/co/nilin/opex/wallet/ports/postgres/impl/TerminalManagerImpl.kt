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
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Component
class TerminalManagerImpl(
    private val terminalRepository: TerminalRepository,
    private val terminalLocalizationsRepository: TerminalLocalizationsRepository,
    private val transactionalOperator: TransactionalOperator

) : TerminalManager {
    private val logger = LoggerFactory.getLogger(TerminalManagerImpl::class.java)

    override suspend fun save(terminalCommand: TerminalCommand, applicator: String? = null): TerminalCommand? {
        return transactionalOperator.executeAndAwait {

            terminalRepository.findByIdentifier(terminalCommand.identifier)
                ?.awaitSingleOrNull()
                ?.let { throw OpexError.TerminalIsExist.exception() }

            val terminal = terminalRepository
                .save(terminalCommand.copy(applicator = applicator).toModel())
                .awaitSingleOrNull()
                ?: return@executeAndAwait null

            val terminalId = terminal.id ?: return@executeAndAwait null


            if (!terminalCommand.description.isNullOrBlank() || !terminalCommand.owner.isNullOrBlank()) {
                terminalLocalizationsRepository.save(
                    TerminalLocalizationModel(
                        terminalId = terminalId,
                        description = terminalCommand.description,
                        owner = terminalCommand.owner,
                        language = getDefaultUserLanguage(),
                        applicator = applicator
                    )
                ).awaitSingleOrNull()
            }

            terminalCommand.apply {
                uuid = terminal.uuid
                this.applicator = terminal.applicator
            }
        }
    }

    override suspend fun update(terminalCommand: TerminalCommand, applicator: String? = null): TerminalCommand? {
        loadTerminal(terminalCommand.uuid!!)?.let {
            val terminal = terminalRepository
                .save(terminalCommand.copy(applicator = applicator).toModel().apply { id = it.id })
                .awaitSingleOrNull()
                ?: return null
            return terminalRepository.findByUuid(terminal.uuid!!, getDefaultUserLanguage())
                ?.awaitSingleOrNull()
                ?.toCommand()
        } ?: throw OpexError.TerminalNotFound.exception()
    }

    override suspend fun delete(uuid: String, applicator: String? = null) {
        val terminal = loadTerminal(uuid) ?: throw OpexError.TerminalNotFound.exception()
        terminalRepository.deleteById(terminal.id!!).awaitSingleOrNull()
        logger.info("Admin {} deleted terminal {}", applicator, uuid)
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
        terminalLocalizations: List<TerminalLocalizationCommand>,
        applicator: String? = null
    ): List<TerminalLocalizationCommand> {

        return transactionalOperator.executeAndAwait {

            val terminal = loadTerminal(terminalUuid)
                ?: throw OpexError.TerminalNotFound.exception()

            terminalLocalizations.forEach { t ->
                if (!t.description.isNullOrBlank() || !t.owner.isNullOrBlank())
                    terminalLocalizationsRepository.upsert(
                        terminalId = terminal.id!!,
                        description = t.description,
                        owner = t.owner,
                        language = UserLanguage.safeValueOf(t.language).toString(),
                        applicator = applicator
                    ).awaitSingleOrNull()
            }

            terminalLocalizationsRepository.findByTerminalId(terminal.id!!)
                .map { it.toCommand() }
                .collectList()
                .awaitSingleOrNull()
                ?: emptyList()
        } ?: throw OpexError.BadRequest.exception("Failed to save terminal localizations")
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

    override suspend fun deleteTerminalLocalizations(id: Long, applicator: String? = null) {
        terminalLocalizationsRepository.deleteById(id).awaitSingleOrNull()
        logger.info("Admin {} deleted terminal localization {}", applicator, id)
    }

    private suspend fun loadTerminal(uuid: String): TerminalModel? {
        return terminalRepository.findByUuid(
            uuid,
            UserLanguage.safeValueOf(getUserLanguage().awaitSingleOrNull()).toString()
        )?.awaitSingleOrNull()
            ?.toModel()

    }
}
