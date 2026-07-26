package co.nilin.opex.bcgateway.core.service

import co.nilin.opex.bcgateway.core.api.AssignAddressService
import co.nilin.opex.bcgateway.core.model.AddressStatus
import co.nilin.opex.bcgateway.core.model.AssignedAddress
import co.nilin.opex.bcgateway.core.spi.AssignedAddressHandler
import co.nilin.opex.bcgateway.core.spi.ChainLoader
import co.nilin.opex.bcgateway.core.spi.CryptoCurrencyHandlerV2
import co.nilin.opex.bcgateway.core.spi.ReservedAddressHandler
import co.nilin.opex.bcgateway.core.utils.LoggerDelegate
import co.nilin.opex.common.OpexError
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

open class AssignAddressServiceImpl(
    private val currencyHandler: CryptoCurrencyHandlerV2,
    private val assignedAddressHandler: AssignedAddressHandler,
    private val reservedAddressHandler: ReservedAddressHandler,
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
        val addressTypes = requestedChain?.addressTypes ?: throw OpexError.BadRequest.exception()

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

    override suspend fun findHolder(address: String, memo: String?, time: Long?): Pair<String?, AddressStatus?> {
        val at: LocalDateTime? = time?.let { ts ->
            val instant = Instant.ofEpochMilli(ts)
            LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        }
        return assignedAddressHandler.findHolder(address, memo, at)
    }

}
