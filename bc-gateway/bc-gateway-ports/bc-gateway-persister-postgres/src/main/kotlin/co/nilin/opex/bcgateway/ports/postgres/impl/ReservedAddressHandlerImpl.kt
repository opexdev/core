package co.nilin.opex.bcgateway.ports.postgres.impl

import co.nilin.opex.bcgateway.core.model.AddressType
import co.nilin.opex.bcgateway.core.model.ReservedAddress
import co.nilin.opex.bcgateway.core.model.ReservedAddressFilter
import co.nilin.opex.bcgateway.core.model.ReservedAddressRecord
import co.nilin.opex.bcgateway.core.spi.AddressTypeHandler
import co.nilin.opex.bcgateway.core.spi.ReservedAddressHandler
import co.nilin.opex.bcgateway.ports.postgres.dao.ReservedAddressRepository
import co.nilin.opex.bcgateway.ports.postgres.model.ReservedAddressModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
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

    override suspend fun claimNext(addressType: AddressType): ReservedAddress? {
        val model = reservedAddressRepository.claimByType(addressType.id).awaitFirstOrNull() ?: return null
        return ReservedAddress(model.address, model.memo, addressType)
    }

    override suspend fun claimAny(): ReservedAddress? {
        val model = reservedAddressRepository.claimAny().awaitFirstOrNull() ?: return null
        val type = addressTypeHandler.fetchAll().firstOrNull { it.id == model.type } ?: return null
        return ReservedAddress(model.address, model.memo, type)
    }

    override suspend fun claimByAddress(address: String, memo: String?): ReservedAddress? {
        // reserved_addresses.memo is NOT NULL, so an absent memo means the empty string
        val model = reservedAddressRepository.claimByAddress(address, memo ?: "").awaitFirstOrNull() ?: return null
        val type = addressTypeHandler.fetchAll().firstOrNull { it.id == model.type } ?: return null
        return ReservedAddress(model.address, model.memo, type)
    }

    override suspend fun findByAddress(address: String, memo: String?): ReservedAddress? {
        val model = reservedAddressRepository.findByAddressAndMemo(address, memo).awaitFirstOrNull() ?: return null
        val type = addressTypeHandler.fetchAll().firstOrNull { it.id == model.type } ?: return null
        return ReservedAddress(model.address, model.memo, type)
    }

    override suspend fun search(filter: ReservedAddressFilter): List<ReservedAddressRecord> {
        val addressTypes = addressTypeHandler.fetchAll().associateBy { it.id }
        return reservedAddressRepository.searchReserved(
            addressTypeId = filter.addressTypeId,
            chain = filter.chain,
            address = filter.address,
            limit = filter.limit,
            offset = filter.offset
        ).map { ReservedAddressRecord(it.address, it.memo, addressTypes[it.type]) }.toList()
    }

    override suspend fun count(filter: ReservedAddressFilter): Long {
        return reservedAddressRepository.countReserved(
            addressTypeId = filter.addressTypeId,
            chain = filter.chain,
            address = filter.address
        ).awaitFirstOrNull() ?: 0
    }
}
