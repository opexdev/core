package co.nilin.opex.kyc.app.service


import co.nilin.opex.profile.core.data.profile.KYCLevel
import org.springframework.stereotype.Component
import co.nilin.opex.profile.core.data.profile.UserCreatedEvent
import data.UpdateKYCLevelRequest
import org.slf4j.LoggerFactory
import spi.KYCPersister
import java.time.LocalDateTime

@Component
class KycManagement(
        private val kycPersister: KYCPersister
) {
    private val logger = LoggerFactory.getLogger(KycManagement::class.java)
    suspend fun updateKycLevel(event: UserCreatedEvent) {
        with(event) {
            kycPersister.createOrUpdateKYCLevel(UpdateKYCLevelRequest(userId = event.uuid, kycLevel = KYCLevel.Level1, reason = "registerUser", lastUpdateDate = LocalDateTime.now()))
        }
    }
    suspend fun updateKycLevel(request: UpdateKYCLevelRequest) {

            kycPersister.createOrUpdateKYCLevel(request)

    }

}



