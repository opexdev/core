package co.nilin.opex.bcgateway.ports.postgres.impl

import co.nilin.opex.bcgateway.core.model.AddressStatus
import co.nilin.opex.bcgateway.core.model.ReservedAddress
import co.nilin.opex.bcgateway.core.spi.AddressManager
import co.nilin.opex.bcgateway.core.spi.CurrencyHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AddressManagerImpl(private val addressHandlerImpl: AssignedAddressHandlerImpl,
                         private val reservedAddressHandlerImpl: ReservedAddressHandlerImpl) : AddressManager {
    private val logger = LoggerFactory.getLogger(AddressManagerImpl::class.java)

    override suspend fun revokeExpiredAddress() {
        addressHandlerImpl.fetchExpiredAssignedAddresses()?.map {
            logger.info("000000000000000")
            logger.info(it.address)
            addressHandlerImpl.revoke(it.apply {
                status = AddressStatus.Revoked
            })
            reservedAddressHandlerImpl.addReservedAddress(listOf(ReservedAddress(it.address, it.memo, it.type)))

        }

    }

}