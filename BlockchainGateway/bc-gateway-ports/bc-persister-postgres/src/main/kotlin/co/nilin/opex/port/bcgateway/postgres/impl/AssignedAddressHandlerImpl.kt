package co.nilin.opex.port.bcgateway.postgres.impl

import co.nilin.opex.bcgateway.core.model.AddressType
import co.nilin.opex.bcgateway.core.model.AssignedAddress
import co.nilin.opex.bcgateway.core.spi.AssignedAddressHandler
import co.nilin.opex.bcgateway.core.spi.ChainLoader
import co.nilin.opex.port.bcgateway.postgres.dao.AddressTypeRepository
import co.nilin.opex.port.bcgateway.postgres.dao.AssignedAddressChainRepository
import co.nilin.opex.port.bcgateway.postgres.dao.AssignedAddressRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.stereotype.Service

@Service
class AssignedAddressHandlerImpl(
    val assignedAddressRepository: AssignedAddressRepository,
    val addressTypeRepository: AddressTypeRepository,
    val assignedAddressChainRepository: AssignedAddressChainRepository,
    val chainLoader: ChainLoader
) : AssignedAddressHandler {
    override suspend fun fetchAssignedAddresses(user: String, addressTypes: List<AddressType>): List<AssignedAddress> {
        return assignedAddressRepository.findByUuidAndAddressType(
            user, addressTypes.map(AddressType::id)
        )
            .map { model ->
                AssignedAddress(
                    model.uuid, model.address, model.memo,
                    addressTypeRepository
                        .findById(model.addressTypeId)
                        .map { aam ->
                            AddressType(aam.id!!, aam.type, aam.addressRegex, aam.memoRegex)
                        }
                        .awaitFirst(),
                    assignedAddressChainRepository.findByAssignedAddress(model.id!!)
                        .map { cm ->
                            chainLoader.fetchChainInfo(cm.chain)
                        }
                        .toList().toMutableList()
                )
            }.toList()
    }

    override suspend fun persist(assignedAddress: AssignedAddress) {
        TODO("Not yet implemented")
    }

    override suspend fun findUuid(address: String, memo: String?): String? {
        TODO("Not yet implemented")
    }
}