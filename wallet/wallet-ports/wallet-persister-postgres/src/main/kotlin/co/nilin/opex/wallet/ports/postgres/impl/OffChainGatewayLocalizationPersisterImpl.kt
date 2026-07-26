package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.common.data.UserLanguage
import co.nilin.opex.wallet.core.model.OffChainGatewayLocalizationCommand
import co.nilin.opex.wallet.core.spi.OffChainGatewayLocalizationPersister
import co.nilin.opex.wallet.ports.postgres.dao.OffChainGatewayLocalizationRepository
import co.nilin.opex.wallet.ports.postgres.dao.OffChainGatewayRepository
import co.nilin.opex.wallet.ports.postgres.util.toCommand
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Component
class OffChainGatewayLocalizationPersisterImpl(
    private val offChainGatewayRepository: OffChainGatewayRepository,
    private val offChainGatewayLocalizationRepository: OffChainGatewayLocalizationRepository,
    private val transactionalOperator: TransactionalOperator
) : OffChainGatewayLocalizationPersister {

    override suspend fun save(
        gatewayUuid: String,
        localizations: List<OffChainGatewayLocalizationCommand>
    ): List<OffChainGatewayLocalizationCommand> {

        return transactionalOperator.executeAndAwait {

            val gateway = offChainGatewayRepository.findByGatewayUuid(gatewayUuid)?.awaitSingleOrNull()
                ?: throw OpexError.GatewayNotFount.exception()

            localizations.forEach { g ->
                if (!g.depositDescription.isNullOrBlank() || !g.withdrawDescription.isNullOrBlank())
                    offChainGatewayLocalizationRepository.upsert(
                        gatewayId = gateway.id!!,
                        depositDescription = g.depositDescription,
                        withdrawDescription = g.withdrawDescription,
                        language = UserLanguage.safeValueOf(g.language).toString()
                    ).awaitSingleOrNull()
            }

            offChainGatewayLocalizationRepository.findByGatewayId(gateway.id!!)
                .map { it.toCommand() }
                .collectList()
                .awaitSingleOrNull()
                ?: emptyList()
        } ?: throw OpexError.BadRequest.exception("Failed to save gateway localizations")
    }

    override suspend fun fetch(gatewayUuid: String): List<OffChainGatewayLocalizationCommand> {
        val gateway = offChainGatewayRepository.findByGatewayUuid(gatewayUuid)?.awaitSingleOrNull()
            ?: throw OpexError.GatewayNotFount.exception()
        return offChainGatewayLocalizationRepository.findByGatewayId(gateway.id!!)
            .map { it.toCommand() }
            .collectList()
            .awaitSingleOrNull()
            ?: emptyList()
    }

    override suspend fun delete(id: Long) {
        offChainGatewayLocalizationRepository.deleteById(id).awaitSingleOrNull()
    }
}