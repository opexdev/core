package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.AssignResponse

interface BlockchainGatewayProxy {

    suspend fun assignAddress(uuid: String, currency: String): AssignResponse

}