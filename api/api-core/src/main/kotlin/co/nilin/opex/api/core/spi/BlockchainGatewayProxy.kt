package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.AssignResponse
import co.nilin.opex.api.core.inout.DepositDetails

interface BlockchainGatewayProxy {

    suspend fun assignAddress(uuid: String, currency: String): AssignResponse?

    suspend fun getDepositDetails(refs: List<String>): List<DepositDetails>

}