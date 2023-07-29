package co.nilin.opex.kyc.app.service


import co.nilin.opex.profile.core.data.profile.KYCLevel
import co.nilin.opex.profile.core.data.profile.Profile
import co.nilin.opex.profile.core.spi.ProfilePersister
import org.springframework.stereotype.Component
import co.nilin.opex.profile.core.data.profile.UserCreatedEvent
import co.nilin.opex.profile.core.data.profile.UserStatus
import data.UpdateKYCLevelRequest
import org.slf4j.LoggerFactory
import spi.KYCPersister
import java.time.LocalDateTime
import java.util.UUID

@Component
class KYCManagement(
        private val kycPersister: KYCPersister
) {
    private val logger = LoggerFactory.getLogger(KYCManagement::class.java)
    suspend fun registerNewUser(event: UserCreatedEvent) {
        with(event) {
            kycPersister.createOrUpdateKYCLevel(UpdateKYCLevelRequest(userId = event.uuid, kycLevel = KYCLevel.Level1, reason = "registerUser", lastUpdateDate = LocalDateTime.now()))
        }
    }


}



