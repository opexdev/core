package spi

import data.UpdateKYCLevelRequest

interface KYCPersister {

    suspend fun createOrUpdateKYCLevel(kycUpdateRequest: UpdateKYCLevelRequest)

}