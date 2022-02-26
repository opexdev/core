package co.nilin.opex.bcgateway.core.service

import co.nilin.opex.bcgateway.core.api.AssignAddressService
import co.nilin.opex.bcgateway.core.model.AddressType
import co.nilin.opex.bcgateway.core.model.AssignedAddress
import co.nilin.opex.bcgateway.core.model.Chain
import co.nilin.opex.bcgateway.core.model.Currency
import co.nilin.opex.bcgateway.core.spi.AssignedAddressHandler
import co.nilin.opex.bcgateway.core.spi.CurrencyHandler
import co.nilin.opex.bcgateway.core.spi.ReservedAddressHandler
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException

class AssignAddressServiceImpl(
    val currencyHandler: CurrencyHandler,
    val assignedAddressHandler: AssignedAddressHandler,
    val reservedAddressHandler: ReservedAddressHandler
) : AssignAddressService {

    override suspend fun assignAddress(user: String, currency: Currency): List<AssignedAddress> {
        val currencyInfo = currencyHandler.fetchCurrencyInfo(currency.symbol)
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
                val reservedAddress = reservedAddressHandler.peekReservedAddress(addressType)
                if (reservedAddress != null) {
                    val newAssigned = AssignedAddress(
                        user,
                        reservedAddress.address,
                        reservedAddress.memo,
                        addressType,
                        chainAddressTypeMap.get(addressType)!!
                    )
                    reservedAddressHandler.remove(reservedAddress)
                    result.add(newAssigned)
                } else {
                    throw OpexException(
                        OpexError.ReservedAddressNotAvailable,
                        "No reserved address available for $addressType"
                    )
                }

            }
        }
        result.forEach { address ->
            assignedAddressHandler.persist(address)
        }
        return result.toMutableList()
    }
}