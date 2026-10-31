package co.nilin.opex.bcgateway.core.api

import co.nilin.opex.bcgateway.core.model.AddressAssignmentFilter
import co.nilin.opex.bcgateway.core.model.AddressAssignmentPage
import co.nilin.opex.bcgateway.core.model.AddressAssignmentRecord
import co.nilin.opex.bcgateway.core.model.AdminAssignAddressRequest
import co.nilin.opex.bcgateway.core.model.ReservedAddressFilter
import co.nilin.opex.bcgateway.core.model.ReservedAddressPage

interface AddressAdminService {

    suspend fun search(filter: AddressAssignmentFilter): AddressAssignmentPage

    suspend fun findById(id: Long): AddressAssignmentRecord

    suspend fun listAvailableAddresses(filter: ReservedAddressFilter): ReservedAddressPage

    suspend fun assign(request: AdminAssignAddressRequest): AddressAssignmentRecord

    suspend fun revoke(id: Long): AddressAssignmentRecord
}
