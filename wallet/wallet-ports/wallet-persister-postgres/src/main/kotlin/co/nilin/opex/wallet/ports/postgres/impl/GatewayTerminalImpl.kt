package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.inout.TerminalCommand
import co.nilin.opex.wallet.core.spi.GatewayTerminalManager
import co.nilin.opex.wallet.ports.postgres.dao.TerminalRepository
import co.nilin.opex.wallet.ports.postgres.dao.GatewayTerminalRepository
import co.nilin.opex.wallet.ports.postgres.dao.OffChainGatewayRepository
import co.nilin.opex.wallet.ports.postgres.model.GatewayTerminalModel
import co.nilin.opex.wallet.ports.postgres.util.toDto
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class GatewayTerminalImpl(
    private val gatewayRepository: OffChainGatewayRepository,
    private val gatewayTerminalRepository: GatewayTerminalRepository,
    private val terminalRepository: TerminalRepository
) : GatewayTerminalManager {
    private val logger = LoggerFactory.getLogger(GatewayTerminalImpl::class.java)

    override suspend fun assignTerminalsToGateway(gatewayUuid: String, terminals: List<String>) {
        gatewayRepository.findByGatewayUuid(gatewayUuid)?.awaitSingleOrNull()?.let { gateway ->
            terminals.forEach { it ->
                terminalRepository.findByUuid(
                    it
                )?.awaitSingleOrNull()?.let {
                    runCatching {
                        gatewayTerminalRepository.save(GatewayTerminalModel(null, it.id!!, gateway.id!!))
                            ?.awaitSingleOrNull()
                    }

                }
            }

        } ?: throw OpexError.GatewayNotFount.exception()
    }

    override suspend fun getAssignedTerminalToGateway(gatewayUuid: String): List<TerminalCommand>? {
        return gatewayRepository.findByGatewayUuid(gatewayUuid)?.awaitSingleOrNull()?.let { gateway ->
            gatewayTerminalRepository.findByGatewayId(gateway.id!!)?.map { it.toDto() }?.collectList()
                ?.awaitSingleOrNull()
        } ?: throw OpexError.GatewayNotFount.exception()
    }

    override suspend fun revokeTerminalsToGateway(gatewayUuid: String, terminals: List<String>) {
        gatewayRepository.findByGatewayUuid(gatewayUuid)?.awaitSingleOrNull()?.let { gateway ->
            terminals.forEach { it ->
                terminalRepository.findByUuid(
                    it
                )?.awaitSingleOrNull()?.let {
                    runCatching {
                        gatewayTerminalRepository.deleteByTerminalIdAndGatewayId(it.id!!, gateway.id!!)
                            ?.awaitSingleOrNull()
                    }

                }
            }

        } ?: throw OpexError.GatewayNotFount.exception()
    }

}