package co.nilin.opex.bcgateway.ports.postgres.impl

import co.nilin.opex.bcgateway.core.model.AddressStatus
import co.nilin.opex.bcgateway.core.model.AddressType
import co.nilin.opex.bcgateway.core.model.AssignedAddress
import co.nilin.opex.bcgateway.core.spi.AssignedAddressHandler
import co.nilin.opex.bcgateway.core.spi.ChainLoader
import co.nilin.opex.bcgateway.ports.postgres.dao.AddressTypeRepository
import co.nilin.opex.bcgateway.ports.postgres.dao.AssignedAddressChainRepository
import co.nilin.opex.bcgateway.ports.postgres.dao.AssignedAddressRepository
import co.nilin.opex.bcgateway.ports.postgres.model.AssignedAddressModel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AssignedAddressHandlerImpl(
        val assignedAddressRepository: AssignedAddressRepository,
        val addressTypeRepository: AddressTypeRepository,
        val assignedAddressChainRepository: AssignedAddressChainRepository,
        val chainLoader: ChainLoader
) : AssignedAddressHandler {
    @Value("\${app.address.life-time.value}")
    private var lifeTime: Long? = null
    @Value("\${app.address.life-time.unit}")
    private var lifeUnit: String? = "minute"
    override suspend fun fetchAssignedAddresses(user: String, addressTypes: List<AddressType>): List<AssignedAddress> {
        if (addressTypes.isEmpty()) return emptyList()
        val addressTypeMap = addressTypeRepository.findAll().map { aam ->
            AddressType(aam.id!!, aam.type, aam.addressRegex, aam.memoRegex)
        }.collectMap { it.id }.awaitFirst()
        return assignedAddressRepository.findByUuidAndAddressTypeAndStatus(
                user, addressTypes.map(AddressType::id), AddressStatus.Reserved
        ).map { model ->
            model.toDto(addressTypeMap)
        }.toList()
    }

    override suspend fun persist(assignedAddress: AssignedAddress) {
        runCatching {
            assignedAddressRepository.save(
                    AssignedAddressModel(
                            null,
                            assignedAddress.uuid,
                            assignedAddress.address,
                            assignedAddress.memo,
                            assignedAddress.type.id,
                            lifeTime?.let { if (lifeUnit == "minute") (LocalDateTime.now().plusMinutes(lifeTime!!)) else null }
                                    ?: null,
                            LocalDateTime.now(),
                            assignedAddress.status
                    )
            ).awaitFirst()
        }
    }


    override suspend fun revoke(assignedAddress: AssignedAddress) {

            assignedAddressRepository.save(
                    AssignedAddressModel(
                            null,
                            assignedAddress.uuid,
                            assignedAddress.address,
                            assignedAddress.memo,
                            assignedAddress.type.id,
                             null,
                            LocalDateTime.now(),
                            assignedAddress.status
                    )
            ).awaitFirst()

    }

    override suspend fun findUuid(address: String, memo: String?): String? {
        return assignedAddressRepository.findByAddressAndMemo(address, memo).awaitFirstOrNull()?.uuid
    }

    override suspend fun fetchExpiredAssignedAddresses(): List<AssignedAddress>? {
        val now = LocalDateTime.now()
        val addressTypeMap = addressTypeRepository.findAll().map { aam ->
            AddressType(aam.id!!, aam.type, aam.addressRegex, aam.memoRegex)
        }.collectMap { it.id }.awaitFirst()
        //to do just revoked address
        return assignedAddressRepository.findPotentialExpAddress(if (lifeUnit == "minute") (now.minusMinutes(lifeTime!!)) else null, now, AddressStatus.Assigned)?.filter {
            it.expTime != null
        }?.map { it.toDto(addressTypeMap) }?.toList()
    }

    private suspend fun AssignedAddressModel.toDto(addressTypeMap: MutableMap<Long, AddressType>): AssignedAddress {
        return AssignedAddress(
                this.uuid,
                this.address,
                this.memo,
                addressTypeMap.getValue(this.addressTypeId),
                assignedAddressChainRepository.findByAssignedAddress(this.id!!).map { cm ->
                    chainLoader.fetchChainInfo(cm.chain)
                }.toList().toMutableList()
        )
    }
}