package co.nilin.opex.bcgateway.core.service

import co.nilin.opex.bcgateway.core.api.AssignAddressService
import co.nilin.opex.bcgateway.core.model.*
import co.nilin.opex.bcgateway.core.spi.AssignedAddressHandler
import co.nilin.opex.bcgateway.core.spi.CurrencyHandler
import co.nilin.opex.bcgateway.core.spi.ReservedAddressHandler
import co.nilin.opex.bcgateway.core.utils.LoggerDelegate
import co.nilin.opex.common.OpexError
import co.nilin.opex.utility.error.data.OpexException
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

open class AssignAddressServiceImpl(
        private val currencyHandler: CurrencyHandler,
        private val assignedAddressHandler: AssignedAddressHandler,
        private val reservedAddressHandler: ReservedAddressHandler
) : AssignAddressService {
    @Value("\${app.address.life-time.value}")
    private var lifeTime: Long? = null
    private val logger: Logger by LoggerDelegate()

    @Transactional
    override suspend fun assignAddress(user: String, currency: Currency, chain: String): List<AssignedAddress> {
        logger.info(ZoneId.systemDefault().toString())
        val currencyInfo = currencyHandler.fetchCurrencyInfo(currency.symbol)
        val chains = currencyInfo.implementations
                .map { imp -> imp.chain }
                .filter { it.name.equals(chain, true) }
        val addressTypes = chains
                .flatMap { chain -> chain.addressTypes }
                .distinct()
        val chainAddressTypeMap = HashMap<AddressType, MutableList<Chain>>()
        chains.forEach { chain ->
            chain.addressTypes.forEach { addressType ->
                chainAddressTypeMap.putIfAbsent(addressType, mutableListOf())
                chainAddressTypeMap.getValue(addressType).add(chain)
            }
        }
        val userAssignedAddresses = (assignedAddressHandler.fetchAssignedAddresses(user, addressTypes)).toMutableList()
        val result = mutableSetOf<AssignedAddress>()
        addressTypes.forEach { addressType ->
            val assigned = userAssignedAddresses.firstOrNull { assignAddress -> assignAddress.type == addressType }
            if (assigned != null) {
                chainAddressTypeMap[addressType]?.forEach { chain ->
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
                            chainAddressTypeMap[addressType]!!,
                            lifeTime?.let { LocalDateTime.now().plusSeconds(lifeTime!!) }
                                    ?: null,
                            LocalDateTime.now(),
                            null,
                            AddressStatus.Assigned,
                            null
                    )
                    reservedAddressHandler.remove(reservedAddress)
                    result.add(newAssigned)
                } else {
                    logger.info("No reserved address available for $addressType")
                    throw OpexError.ReservedAddressNotAvailable.exception()
                }

            }
        }
        result.forEach { address ->
            assignedAddressHandler.persist(address)
            address.apply { id=null }
        }
        return result.toMutableList()
    }

}
