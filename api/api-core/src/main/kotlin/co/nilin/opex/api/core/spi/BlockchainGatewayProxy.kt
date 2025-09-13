package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.AssignAddressRequest
import co.nilin.opex.api.core.inout.AssignResponse
import co.nilin.opex.api.core.inout.DepositDetails

interface BlockchainGatewayProxy {

    fun assignAddress(assignAddressRequest: AssignAddressRequest): AssignResponse?

    fun getDepositDetails(refs: List<String>): List<DepositDetails>
}