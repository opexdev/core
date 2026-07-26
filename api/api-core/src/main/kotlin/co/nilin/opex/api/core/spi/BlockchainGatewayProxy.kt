package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.AssignAddressRequest
import co.nilin.opex.api.core.inout.AssignResponse
import co.nilin.opex.api.core.inout.ChainInfo
import co.nilin.opex.api.core.inout.DepositDetails
import co.nilin.opex.api.core.inout.GatewayLocalizationCommand
import co.nilin.opex.api.core.inout.GatewayLocalizationResponse

interface BlockchainGatewayProxy {

    suspend fun assignAddress(assignAddressRequest: AssignAddressRequest): AssignResponse?
    suspend fun getDepositDetails(refs: List<String>): List<DepositDetails>
    suspend fun getChainInfo(): List<ChainInfo>

    suspend fun getOnChainGatewayLocalizations(token: String, gatewayUuid: String): GatewayLocalizationResponse
    suspend fun saveOnChainGatewayLocalizations(
        token: String,
        gatewayUuid: String,
        gatewayLocalizations: List<GatewayLocalizationCommand>
    ): GatewayLocalizationResponse

    suspend fun deleteOnChainGatewayLocalization(token: String, id: Long)
}