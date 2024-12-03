package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.TerminalCommand

interface GatewayTerminalManager {
    suspend fun assignTerminalToGateway(gatewayUuid: String, terminal: List<String>)

    suspend fun getAssignedTerminalToGateway(gatewayUuid: String): List<TerminalCommand>?

    suspend fun revokeTerminalToGateway(gatewayUuid: String, terminal: List<String>)

}