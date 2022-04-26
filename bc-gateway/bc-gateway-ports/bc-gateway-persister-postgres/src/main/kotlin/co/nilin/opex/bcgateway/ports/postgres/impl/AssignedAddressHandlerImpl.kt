package co.nilin.opex.bcgateway.ports.postgres.impl

import co.nilin.opex.bcgateway.core.model.AddressType
import co.nilin.opex.bcgateway.core.model.AssignedAddress
import co.nilin.opex.bcgateway.core.spi.AssignedAddressHandler
import co.nilin.opex.bcgateway.core.spi.ChainLoader
import co.nilin.opex.bcgateway.ports.postgres.dao.AddressTypeRepository
import co.nilin.opex.bcgateway.ports.postgres.dao.AssignedAddressChainRepository
import co.nilin.opex.bcgateway.ports.postgres.dao.AssignedAddressRepository
import co.nilin.opex.bcgateway.ports.postgres.model.AssignedAddressModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class AssignedAddressHandlerImpl(
    val assignedAddressRepository: AssignedAddressRepository,
    val addressTypeRepository: AddressTypeRepository,
    val assignedAddressChainRepository: AssignedAddressChainRepository,
    val chainLoader: ChainLoader
) : AssignedAddressHandler {
    override suspend fun fetchAssignedAddresses(user: String, addressTypes: List<AddressType>): List<AssignedAddress> {
        if (addressTypes.isEmpty()) return emptyList()
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
        try {
            assignedAddressRepository.save(
                AssignedAddressModel(
                    null,
                    assignedAddress.uuid,
                    assignedAddress.address,
                    assignedAddress.memo?.lowercase(),
                    assignedAddress.type.id
                )
            ).awaitFirst()
        } catch (e: Exception) {

        }
    }

    override suspend fun findUuid(address: String, memo: String?): String? {
        return assignedAddressRepository.findByAddressAndMemo(address, memo).awaitFirstOrNull()?.uuid
    }
}