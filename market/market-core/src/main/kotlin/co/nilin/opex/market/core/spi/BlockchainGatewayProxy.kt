package co.nilin.opex.market.core.spi

import co.nilin.opex.market.core.inout.AssignResponse
import co.nilin.opex.market.core.inout.DepositDetails

interface BlockchainGatewayProxy {

    suspend fun assignAddress(uuid: String, currency: String): AssignResponse?

    suspend fun getDepositDetails(refs: List<String>): List<DepositDetails>

}