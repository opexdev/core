package co.nilin.opex.bcgateway.ports.postgres.impl

import co.nilin.opex.bcgateway.core.model.AddressStatus
import co.nilin.opex.bcgateway.core.model.AddressType
import co.nilin.opex.bcgateway.core.model.AssignedAddress
import co.nilin.opex.bcgateway.core.model.AssignedAddressV2
import co.nilin.opex.bcgateway.core.spi.AssignedAddressHandler
import co.nilin.opex.bcgateway.core.spi.ChainLoader
import co.nilin.opex.bcgateway.core.utils.LoggerDelegate
import co.nilin.opex.bcgateway.ports.postgres.dao.AddressTypeRepository
import co.nilin.opex.bcgateway.ports.postgres.dao.AssignedAddressChainRepository
import co.nilin.opex.bcgateway.ports.postgres.dao.AssignedAddressRepository
import co.nilin.opex.bcgateway.ports.postgres.model.AssignedAddressModel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class AssignedAddressHandlerImpl(
        val assignedAddressRepository: AssignedAddressRepository,
        val addressTypeRepository: AddressTypeRepository,
        val assignedAddressChainRepository: AssignedAddressChainRepository,
        val chainLoader: ChainLoader
) : AssignedAddressHandler {
    @Value("\${app.address.life-time.value}")
    private var lifeTime: Long? = null

    private val logger: Logger by LoggerDelegate()

    override suspend fun fetchAssignedAddresses(user: String, addressTypes: List<AddressType>): List<AssignedAddress> {
        if (addressTypes.isEmpty()) return emptyList()
        val addressTypeMap = addressTypeRepository.findAll().map { aam ->
            AddressType(aam.id!!, aam.type, aam.addressRegex, aam.memoRegex)
        }.collectMap { it.id }.awaitFirst()
        return assignedAddressRepository.findByUuidAndAddressTypeAndStatus(
                user, addressTypes.map(AddressType::id), AddressStatus.Assigned
        ).map { model ->
            model.toDto(addressTypeMap).apply { id = model.id }
        }.filter { it.expTime?.let { it > LocalDateTime.now() } ?: true }.toList()
    }


    override suspend fun fetchAssignedAddresses(user: String, addressType: Long): AssignedAddressV2? {
        return assignedAddressRepository.findByUuidAndAddressTypeAndStatus(
                user, addressType, AddressStatus.Assigned
        ).map { model ->
            model.toDto().apply { id = model.id }
        }.filter { it.expTime?.let { it > LocalDateTime.now() } ?: true }.firstOrNull()
    }

    override suspend fun persist(assignedAddress: AssignedAddress) {

        logger.info("going to save new address .............")
        assignedAddressRepository.save(
                AssignedAddressModel(
                        assignedAddress.id ?: null,
                        assignedAddress.uuid,
                        assignedAddress.address,
                        assignedAddress.memo,
                        assignedAddress.type.id,
                        assignedAddress.id?.let { assignedAddress.expTime }
                                ?: (lifeTime?.let { (LocalDateTime.now().plusSeconds(lifeTime!!)) }
                                        ?: null),
                        assignedAddress.id?.let { assignedAddress.assignedDate } ?: LocalDateTime.now(),
                        null,
                        assignedAddress.status
                )
        ).awaitFirstOrNull()
    }


    override suspend fun persist(user:String,assignedAddress: AssignedAddressV2):AssignedAddressV2? {

        logger.info("going to save new address .............")
       return assignedAddressRepository.save(
                AssignedAddressModel(
                        assignedAddress.id ?: null,
                        user,
                        assignedAddress.address,
                        assignedAddress.memo,
                        assignedAddress.typeId,
                        assignedAddress.id?.let { assignedAddress.expTime }
                                ?: (lifeTime?.let { (LocalDateTime.now().plusSeconds(lifeTime!!)) }
                                        ?: null),
                        assignedAddress.id?.let { assignedAddress.assignedDate } ?: LocalDateTime.now(),
                        null,
                        assignedAddress.status
                )
        ).awaitFirstOrNull()?.toDto()
    }


    override suspend fun revoke(assignedAddress: AssignedAddress) {

        assignedAddressRepository.save(
                AssignedAddressModel(
                        assignedAddress.id,
                        assignedAddress.uuid,
                        assignedAddress.address,
                        assignedAddress.memo,
                        assignedAddress.type.id,
                        assignedAddress.expTime,
                        assignedAddress.assignedDate,
                        assignedAddress.revokedDate,
                        assignedAddress.status
                )
        ).awaitFirst()

    }

    override suspend fun findUuid(address: String, memo: String?): String? {
        return assignedAddressRepository.findByAddressAndMemoAndStatus(address, memo, AddressStatus.Assigned).awaitFirstOrNull()?.uuid
    }

    override suspend fun fetchExpiredAssignedAddresses(): List<AssignedAddress>? {
        val now = LocalDateTime.now()
        val addressTypeMap = addressTypeRepository.findAll().map { aam ->
            AddressType(aam.id!!, aam.type, aam.addressRegex, aam.memoRegex)
        }.collectMap { it.id }.awaitFirst()
        //for having significant margin : (minus(5 mints)
        return assignedAddressRepository.findPotentialExpAddress(
                (now.minusSeconds(lifeTime!!)).minusMinutes(5),
                now,
                AddressStatus.Assigned
        )?.filter {
            it.expTime != null
        }?.map {
            it.toDto(addressTypeMap).apply { id = it.id }
        }?.toList()
    }

    private suspend fun AssignedAddressModel.toDto(addressTypeMap: MutableMap<Long, AddressType>): AssignedAddress {
        return AssignedAddress(
                this.uuid,
                this.address,
                this.memo,
                addressTypeMap.getValue(this.addressTypeId),
                assignedAddressChainRepository.findByAssignedAddress(this.id!!).map { cm ->
                    chainLoader.fetchChainInfo(cm.chain)
                }.toList().toMutableList(),
                this.expTime,
                this.assignedDate,
                this.revokedDate,
                this.status,
                null
        )
    }


    private suspend fun AssignedAddressModel.toDto(): AssignedAddressV2 {
        return AssignedAddressV2(
                this.addressTypeId,
                this.address,
                this.memo,
                this.expTime,
                this.assignedDate,
                this.revokedDate,
                this.status,
                null
        )
    }
}