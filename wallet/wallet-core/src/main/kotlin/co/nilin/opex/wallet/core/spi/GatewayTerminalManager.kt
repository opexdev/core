package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.TerminalCommand

interface GatewayTerminalManager {
    suspend fun assignTerminalsToGateway(gatewayUuid: String, terminals: List<String>)

    suspend fun getAssignedTerminalToGateway(gatewayUuid: String): List<TerminalCommand>?

    suspend fun revokeTerminalsToGateway(gatewayUuid: String, terminals: List<String>)

}