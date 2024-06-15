package co.nilin.opex.bcgateway.core.api

import co.nilin.opex.bcgateway.core.model.AssignedAddress
import co.nilin.opex.bcgateway.core.model.AssignedAddressV2

interface AssignAddressService {
    suspend fun assignAddress(user: String, currencyImplUuid:String): List<AssignedAddressV2>
}