package co.nilin.opex.bcgateway.core.service

import co.nilin.opex.bcgateway.core.api.AssignAddressService
import co.nilin.opex.bcgateway.core.model.*
import co.nilin.opex.bcgateway.core.spi.*
import co.nilin.opex.bcgateway.core.utils.LoggerDelegate
import co.nilin.opex.common.OpexError
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

open class AssignAddressServiceImpl(
    private val currencyHandler: CryptoCurrencyHandlerV2,
    private val assignedAddressHandler: AssignedAddressHandler,
    private val reservedAddressHandler: ReservedAddressHandler,
    private val addressTypeHandler: AddressTypeHandler,
    private val chainLoader: ChainLoader

) : AssignAddressService {
    @Value("\${app.address.life-time}")
    private var addressLifeTime: Long? = null
    private val logger: Logger by LoggerDelegate()

    @Transactional
    override suspend fun assignAddress(user: String, currency: String, gatewayUuid: String): List<AssignedAddress> {
        addressLifeTime = 7200
        val requestedGateway = currencyHandler.fetchOnChainGateway(currency = currency, gatewayUuid = gatewayUuid)
            ?: throw OpexError.CurrencyNotFound.exception()

        val requestedChain = chainLoader.fetchChainInfo(requestedGateway.chain)

        val addressTypes = chainLoader.fetchChainInfo(requestedChain.name)?.addressTypes

        val userAssignedAddresses =
            (assignedAddressHandler.fetchAssignedAddresses(user, addressTypes!!)).toMutableList()

        val result = mutableSetOf<AssignedAddress>()

        addressTypes.forEach { addressType ->
            val assigned = userAssignedAddresses.firstOrNull { assignAddress -> assignAddress.type == addressType }
            if (assigned != null) {
                result.add(assigned)
            } else {
                val reservedAddress = reservedAddressHandler.peekReservedAddress(addressType)
                if (reservedAddress != null) {
                    val newAssigned = AssignedAddress(
                        user,
                        reservedAddress.address,
                        reservedAddress.memo,
                        addressType,
                        listOf(requestedChain).toMutableList(),
                        addressLifeTime?.let { LocalDateTime.now().plusSeconds(addressLifeTime!!) }
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
            address.apply { id = null }
        }
        return result.toMutableList()
    }

}
