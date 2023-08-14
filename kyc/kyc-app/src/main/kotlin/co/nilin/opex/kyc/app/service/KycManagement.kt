package co.nilin.opex.kyc.app.service


import co.nilin.opex.kyc.core.data.*
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory
import co.nilin.opex.kyc.core.spi.KYCPersister
import co.nilin.opex.kyc.core.spi.StorageProxy
import kotlinx.coroutines.flow.Flow
import org.springframework.http.codec.multipart.FilePart


@Component
class KycManagement(
        private val kycPersister: KYCPersister,
        private val storageProxy: StorageProxy
) {
    private val logger = LoggerFactory.getLogger(KycManagement::class.java)

    suspend fun kycProcess(kycRequest: KycRequest): KycResponse? {
        return kycPersister.kycProcess(kycRequest)
    }

    suspend fun uploadData(uploadDataRequest: UploadDataRequest): KycResponse? {
        var failsPath = mutableMapOf<String, String>()
        for (data in uploadDataRequest.files!!) {
            val path = storageProxy.uploadFile(data.value , data.key,uploadDataRequest.processId!!).path
            failsPath[data.key] = path
        }
        uploadDataRequest.filesPath=failsPath
        return kycPersister.kycProcess(uploadDataRequest)
    }

    suspend fun manualReview(manualReviewRequest: ManualReviewRequest): KycResponse? {
        return kycPersister.kycProcess(manualReviewRequest)
    }
    suspend fun manualUpdate(manualUpdateRequest: ManualUpdateRequest): KycResponse?{
        return kycPersister.kycProcess(manualUpdateRequest)
    }

    suspend fun getKycData(kycDataRequest: KycDataRequest): Flow<KycProcess>?{
        return kycPersister.getData(kycDataRequest)
    }

}



