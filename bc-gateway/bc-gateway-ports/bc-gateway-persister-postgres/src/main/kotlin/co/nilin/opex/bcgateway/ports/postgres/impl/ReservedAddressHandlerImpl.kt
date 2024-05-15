package co.nilin.opex.bcgateway.ports.postgres.impl

import co.nilin.opex.bcgateway.core.model.AddressType
import co.nilin.opex.bcgateway.core.model.ReservedAddress
import co.nilin.opex.bcgateway.core.model.ReservedAddressV2
import co.nilin.opex.bcgateway.core.spi.AddressTypeHandler
import co.nilin.opex.bcgateway.core.spi.ReservedAddressHandler
import co.nilin.opex.bcgateway.ports.postgres.dao.ReservedAddressRepository
import co.nilin.opex.bcgateway.ports.postgres.model.ReservedAddressModel
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

@Component
class ReservedAddressHandlerImpl(
    private val reservedAddressRepository: ReservedAddressRepository,
    private val addressTypeHandler: AddressTypeHandler
) : ReservedAddressHandler {
    override suspend fun addReservedAddress(list: List<ReservedAddress>) {
        val items = list.map { ReservedAddressModel(null, it.address, it.memo, it.type.id) }
        reservedAddressRepository.saveAll(items).collectList().awaitFirst()
    }

    override suspend fun peekReservedAddress(addressType: AddressType): ReservedAddress? {
        return reservedAddressRepository.peekFirstAdded(addressType.id)
            .map { ReservedAddress(it.address, it.memo, addressType) }.awaitFirstOrNull()
    }


    override suspend fun peekReservedAddress(addressTypeId: Long): ReservedAddressV2? {
        return reservedAddressRepository.peekFirstAdded(addressTypeId)
                .map { ReservedAddressV2(it.address, it.memo) }.awaitFirstOrNull()
    }

    override suspend fun remove(reservedAddress: ReservedAddress) {
        reservedAddressRepository.remove(reservedAddress.address, reservedAddress.memo).awaitFirst()
    }

    override suspend fun remove(reservedAddress: ReservedAddressV2) {
        reservedAddressRepository.remove(reservedAddress.address, reservedAddress.memo).awaitFirst()
    }
}
