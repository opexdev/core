package co.nilin.opex.kyc.ports.postgres.imp

import data.UpdateKYCLevelRequest
import org.springframework.stereotype.Component
import spi.KYCPersister

@Component
class KycManagementImp : KYCPersister {
    override suspend fun createOrUpdateKYCLevel(kycUpdateRequest: UpdateKYCLevelRequest) {
        TODO("Not yet implemented")
    }
}