package co.nilin.opex.kyc.app.service


import co.nilin.opex.kyc.core.data.*
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory
import co.nilin.opex.kyc.core.spi.KYCPersister
import co.nilin.opex.kyc.core.spi.StorageProxy
import kotlinx.coroutines.flow.*
import org.springframework.http.codec.multipart.FilePart
import java.util.Arrays


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
            val path = storageProxy.uploadFile(data.value, data.key, uploadDataRequest.userId!!).path
            failsPath[data.key] = path
        }
        uploadDataRequest.filesPath = failsPath
        return kycPersister.kycProcess(uploadDataRequest)
    }

    suspend fun manualReview(manualReviewRequest: ManualReviewRequest): KycResponse? {
        return kycPersister.kycProcess(manualReviewRequest)
    }

    suspend fun manualUpdate(manualUpdateRequest: ManualUpdateRequest): KycResponse? {
        return kycPersister.kycProcess(manualUpdateRequest)
    }

    suspend fun getKycData(kycDataRequest: KycDataRequest): Flow<KycProcess>? {
        val resp = kycPersister.getData(kycDataRequest)
        return resp?.map { r ->
            var dataInput = ArrayList<String>()
            if (r.step?.name?.lowercase()?.contains("upload") == true) {
                r.input?.split("#")?.forEach { address -> dataInput.add(address) }
                r.data = dataInput
                r.input = null
                r.relatedStep = (resp.filter { data -> (data.referenceId == r.stepId) && (data.stepId != r.stepId) }).toList()
                r
            }
            r
        }
    }
}



