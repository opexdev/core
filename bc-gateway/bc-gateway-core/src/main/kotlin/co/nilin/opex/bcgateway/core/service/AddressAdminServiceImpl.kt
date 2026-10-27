package co.nilin.opex.bcgateway.core.service

import co.nilin.opex.bcgateway.core.api.AddressAdminService
import co.nilin.opex.bcgateway.core.model.AddressAssignmentFilter
import co.nilin.opex.bcgateway.core.model.AddressAssignmentPage
import co.nilin.opex.bcgateway.core.model.AddressAssignmentRecord
import co.nilin.opex.bcgateway.core.model.AddressStatus
import co.nilin.opex.bcgateway.core.model.AdminAssignAddressRequest
import co.nilin.opex.bcgateway.core.model.AssignedAddress
import co.nilin.opex.bcgateway.core.model.ReservedAddress
import co.nilin.opex.bcgateway.core.model.ReservedAddressFilter
import co.nilin.opex.bcgateway.core.model.ReservedAddressPage
import co.nilin.opex.bcgateway.core.spi.AssignedAddressHandler
import co.nilin.opex.bcgateway.core.spi.ReservedAddressHandler
import co.nilin.opex.bcgateway.core.utils.LoggerDelegate
import co.nilin.opex.common.OpexError
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

open class AddressAdminServiceImpl(
    private val assignedAddressHandler: AssignedAddressHandler,
    private val reservedAddressHandler: ReservedAddressHandler
) : AddressAdminService {

    @Value("\${app.address.life-time}")
    private var addressLifeTime: Long? = null

    private val logger: Logger by LoggerDelegate()

    override suspend fun search(filter: AddressAssignmentFilter): AddressAssignmentPage {
        return AddressAssignmentPage(
            total = assignedAddressHandler.count(filter),
            items = assignedAddressHandler.search(filter)
        )
    }

    override suspend fun findById(id: Long): AddressAssignmentRecord {
        return assignedAddressHandler.findRecordById(id) ?: throw OpexError.IdentifierNotFound.exception()
    }

    override suspend fun listAvailableAddresses(filter: ReservedAddressFilter): ReservedAddressPage {
        return ReservedAddressPage(
            total = reservedAddressHandler.count(filter),
            items = reservedAddressHandler.search(filter)
        )
    }

    @Transactional
    override suspend fun assign(request: AdminAssignAddressRequest): AddressAssignmentRecord {
        if (request.uuid.isBlank()) throw OpexError.InvalidRequestBody.exception("uuid is required")
        val reserved = claimAddress(request)
        assignedAddressHandler.findUuid(reserved.address, reserved.memo)?.let { holder ->
            logger.error("Reserved address ${reserved.address} is still assigned to $holder")
            throw OpexError.AddressAlreadyAssigned.exception()
        }

        val expTime = request.expTime?.toLocalDateTime()
            ?: addressLifeTime?.let { LocalDateTime.now().plusSeconds(it) }

        return assignedAddressHandler.persistAssignment(
            AssignedAddress(
                request.uuid,
                reserved.address,
                reserved.memo,
                reserved.type,
                mutableListOf(),
                expTime,
                LocalDateTime.now(),
                null,
                AddressStatus.Assigned,
                null
            )
        )
    }

    @Transactional
    override suspend fun revoke(id: Long): AddressAssignmentRecord {
        val record = findById(id)
        if (record.status != AddressStatus.Assigned)
            throw OpexError.BadRequest.exception("Only an assigned address can be revoked")
        val revoked = assignedAddressHandler.revokeById(id, LocalDateTime.now())
        val type = record.addressType
        if (type == null) {
            logger.warn("Assignment $id has an unknown address type, not returning ${record.address} to the pool")
        } else if (reservedAddressHandler.findByAddress(record.address, record.memo) == null) {
            reservedAddressHandler.addReservedAddress(listOf(ReservedAddress(record.address, record.memo, type)))
        }
        return revoked
    }

    private suspend fun claimAddress(request: AdminAssignAddressRequest): ReservedAddress {
        request.address?.takeIf { it.isNotBlank() }?.let { address ->
            return reservedAddressHandler.claimByAddress(address, request.memo)
                ?: throw OpexError.BadRequest.exception(
                    "Address $address is not available in the reserved pool, pick one from the available list"
                )
        }

        return reservedAddressHandler.claimAny()
            ?: throw OpexError.ReservedAddressNotAvailable.exception()
    }

    private fun Long.toLocalDateTime(): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
}
