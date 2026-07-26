package co.nilin.opex.bcgateway.core.api

import co.nilin.opex.bcgateway.core.model.AddressStatus
import co.nilin.opex.bcgateway.core.model.AssignedAddress

//import co.nilin.opex.bcgateway.core.model.Currency

interface AssignAddressService {
    suspend fun assignAddress(user: String, currency: String, gatewayUuid: String): List<AssignedAddress>
    /**
     * Find the holder (uuid) and status of an address.
     * @param address The on-chain address.
     * @param memo Optional memo/tag for chains that require it.
     * @param time If non-null, represents epoch milliseconds specifying the moment to check; if null, checks current.
     * @return Pair of (uuid at the requested time or current, current status). Both may be null when unassigned.
     */
    suspend fun findHolder(address: String, memo: String? = null, time: Long?): Pair<String?, AddressStatus?>
}