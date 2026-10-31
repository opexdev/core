package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.AddressAssignmentFilter
import co.nilin.opex.api.core.inout.AddressAssignmentPage
import co.nilin.opex.api.core.inout.AddressAssignmentRecord
import co.nilin.opex.api.core.inout.AdminAssignAddressRequest
import co.nilin.opex.api.core.inout.AssignAddressRequest
import co.nilin.opex.api.core.inout.AssignResponse
import co.nilin.opex.api.core.inout.ChainInfo
import co.nilin.opex.api.core.inout.DepositDetails
import co.nilin.opex.api.core.inout.GatewayLocalizationCommand
import co.nilin.opex.api.core.inout.GatewayLocalizationResponse
import co.nilin.opex.api.core.inout.ReservedAddressFilter
import co.nilin.opex.api.core.inout.ReservedAddressPage

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

    // Admin address assignment reports and management
    suspend fun searchAddressAssignments(token: String, filter: AddressAssignmentFilter): AddressAssignmentPage
    suspend fun getAddressAssignment(token: String, id: Long): AddressAssignmentRecord
    suspend fun searchReservedAddresses(token: String, filter: ReservedAddressFilter): ReservedAddressPage
    suspend fun assignAddressByAdmin(token: String, request: AdminAssignAddressRequest): AddressAssignmentRecord
    suspend fun revokeAddressAssignment(token: String, id: Long): AddressAssignmentRecord
}