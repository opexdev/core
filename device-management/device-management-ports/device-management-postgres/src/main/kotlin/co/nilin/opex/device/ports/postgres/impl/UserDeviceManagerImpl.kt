package co.nilin.opex.device.ports.postgres.impl

import co.nilin.opex.device.core.data.UserDevice
import co.nilin.opex.device.core.spi.UserDevicePersister
import co.nilin.opex.device.ports.postgres.dao.UserDeviceRepository
import co.nilin.opex.device.ports.postgres.utils.toDto
import co.nilin.opex.device.ports.postgres.utils.toModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class UserDeviceManagerImpl(
    private val userDeviceRepository: UserDeviceRepository
) : UserDevicePersister {

    private val logger = LoggerFactory.getLogger(UserDeviceManagerImpl::class.java)

    override suspend fun linkDeviceToUser(
        userId: String,
        deviceId: Long,
        firstLoginDate: LocalDateTime
    ): UserDevice? {
        val existing = userDeviceRepository.findByUserIdAndDeviceId(userId, deviceId).awaitFirstOrNull()
        return if (existing != null) {
            // Update firstLoginDate only if null
            val updated = existing.copy(
                firstLoginDate = existing.firstLoginDate ?: firstLoginDate,
                lastLoginDate = LocalDateTime.now()
            )
            userDeviceRepository.save(updated).awaitFirst().toDto()
        } else {
            val newRecord = UserDevice(userId, deviceId, firstLoginDate, LocalDateTime.now())
            userDeviceRepository.save(newRecord.toModel()).awaitFirst().toDto()
        }
    }

    override suspend fun fetchUserDevice(userId: String, deviceId: Long): UserDevice? {
        return userDeviceRepository.findByUserIdAndDeviceId(userId, deviceId)
            .awaitFirstOrNull()
            ?.toDto()
    }

    override suspend fun fetchUserDevices(userId: String): List<UserDevice> {
        return userDeviceRepository.findByUserId(userId)
            .map { it.toDto() }
            .collectList()
            .awaitFirst()
    }

    override suspend fun updateLastUsed(userId: String, deviceId: Long, lastLoginDate: LocalDateTime): Boolean {
        val existing = userDeviceRepository.findByUserIdAndDeviceId(userId, deviceId).awaitFirstOrNull()
        return if (existing != null) {
            val updated = existing.copy(lastLoginDate = lastLoginDate)
            userDeviceRepository.save(updated).awaitFirst()
            true
        } else {
            false
        }
    }

    override suspend fun unlinkDeviceFromUser(userId: String, deviceId: Long): Boolean {
        return try {
            userDeviceRepository.findByUserIdAndDeviceId(userId, deviceId)
                .awaitFirstOrNull()
                ?.let { userDeviceRepository.delete(it).awaitFirstOrNull() }
            true
        } catch (ex: Exception) {
            logger.error("Failed to unlink device $deviceId from user $userId", ex)
            false
        }
    }
}
