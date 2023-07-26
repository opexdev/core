package co.nilin.opex.bcgateway.core.api

import co.nilin.opex.bcgateway.core.model.AssignedAddress
import co.nilin.opex.bcgateway.core.model.Currency

interface AssignAddressService {
    suspend fun assignAddress(user: String, currency: Currency, chain: String): List<AssignedAddress>
}