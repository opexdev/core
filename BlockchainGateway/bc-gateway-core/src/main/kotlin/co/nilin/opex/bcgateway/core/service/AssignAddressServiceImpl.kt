package co.nilin.opex.bcgateway.core.service

import co.nilin.opex.bcgateway.core.api.AssignAddressService
import co.nilin.opex.bcgateway.core.model.AddressType
import co.nilin.opex.bcgateway.core.model.AssignedAddress
import co.nilin.opex.bcgateway.core.model.Chain
import co.nilin.opex.bcgateway.core.model.Currency
import co.nilin.opex.bcgateway.core.spi.AssignedAddressHandler
import co.nilin.opex.bcgateway.core.spi.CachedAddressHandler
import co.nilin.opex.bcgateway.core.spi.CurrencyLoader
import java.lang.RuntimeException

class AssignAddressServiceImpl(
    val currencyLoader: CurrencyLoader,
    val assignedAddressHandler: AssignedAddressHandler,
    val cachedAddressHandler: CachedAddressHandler
) : AssignAddressService {

    override suspend fun assignAddress(user: String, currency: Currency): List<AssignedAddress> {
        val currencyInfo = currencyLoader.fetchCurrencyInfo(currency.symbol)
        val chains = currencyInfo.implementations
            .map { imp -> imp.chain }
        val addressTypes = chains
            .flatMap { chain -> chain.addressTypes }
            .distinct()
        val chainAddressTypeMap = HashMap<AddressType, MutableList<Chain>>()
        chains.forEach { chain ->
            chain.addressTypes.forEach { addressType ->
                chainAddressTypeMap.putIfAbsent(addressType, mutableListOf())
                chainAddressTypeMap.get(addressType)!!.add(chain)
            }
        }
        val userAssignedAddresses = (assignedAddressHandler.fetchAssignedAddresses(user, addressTypes)).toMutableList()
        val result = mutableSetOf<AssignedAddress>()
        addressTypes.forEach { addressType ->
            val assigned = userAssignedAddresses.firstOrNull { assignAddress -> assignAddress.type.equals(addressType) }
            if (assigned != null) {
                chainAddressTypeMap.get(addressType)?.forEach { chain ->
                    if (!assigned.chains.contains(chain)) {
                        assigned.chains.add(chain)
                    }
                }
                result.add(assigned)
            } else {
                val cachedAddress = cachedAddressHandler.peekCachedAddress(addressType)
                if (cachedAddress != null) {
                    val newAssigned = AssignedAddress(
                        user,
                        cachedAddress.address,
                        cachedAddress.memo,
                        addressType,
                        chainAddressTypeMap.get(addressType)!!
                    )
                    cachedAddressHandler.remove(cachedAddress)
                    result.add(newAssigned)
                } else
                    throw RuntimeException("No cached address available for $addressType")

            }
        }
        result.forEach { address ->
            assignedAddressHandler.persist(address)
        }
        return result.toMutableList()
    }
}