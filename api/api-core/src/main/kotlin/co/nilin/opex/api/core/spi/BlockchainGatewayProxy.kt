package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.AssignResponse
import co.nilin.opex.api.core.inout.CurrencyImplementation
import co.nilin.opex.api.core.inout.DepositDetails

interface BlockchainGatewayProxy {

    suspend fun assignAddress(uuid: String, currency: String, chain: String): AssignResponse?

    suspend fun getDepositDetails(refs: List<String>): List<DepositDetails>

//    suspend fun getCurrencyImplementations(currency: String? = null): List<CurrencyImplementation>


}