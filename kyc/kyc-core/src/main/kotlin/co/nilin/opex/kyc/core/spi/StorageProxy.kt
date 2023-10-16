package co.nilin.opex.kyc.core.spi

import co.nilin.opex.kyc.core.data.UploadResult
import org.springframework.http.codec.multipart.FilePart

interface StorageProxy {
    suspend fun uploadFile(file: FilePart, name: String, reference: String): UploadResult
}