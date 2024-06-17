package co.nilin.opex.bcgateway.ports.postgres.impl

import co.nilin.opex.bcgateway.core.model.AddressStatus
import co.nilin.opex.bcgateway.core.model.ReservedAddress
import co.nilin.opex.bcgateway.core.spi.AddressManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class AddressManagerImpl(private val addressHandlerImpl: AssignedAddressHandlerImpl,
                         private val reservedAddressHandlerImpl: ReservedAddressHandlerImpl) : AddressManager {
    private val logger = LoggerFactory.getLogger(AddressManagerImpl::class.java)

    override suspend fun revokeExpiredAddress() {
        addressHandlerImpl.fetchExpiredAssignedAddresses()?.map {
            addressHandlerImpl.revoke(it.apply {
                id=it.id
                status = AddressStatus.Revoked
                revokedDate= LocalDateTime.now()
            })
            reservedAddressHandlerImpl.addReservedAddress(listOf(ReservedAddress(it.address, it.memo, it.type)))

        }

    }

}