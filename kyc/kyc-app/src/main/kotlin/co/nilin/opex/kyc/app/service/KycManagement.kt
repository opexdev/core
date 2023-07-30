package co.nilin.opex.kyc.app.service


import co.nilin.opex.core.data.KycRequest
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory
import co.nilin.opex.core.spi.KYCPersister

@Component
class KycManagement(
        private val kycPersister: KYCPersister
) {
    private val logger = LoggerFactory.getLogger(KycManagement::class.java)

    suspend fun kycProcess(kycRequest: KycRequest) {
        kycPersister.kycProcess(kycRequest)
    }

}



