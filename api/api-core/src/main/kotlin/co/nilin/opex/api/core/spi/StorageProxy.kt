package co.nilin.opex.api.core.spi

import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart

interface StorageProxy {
    suspend fun adminDownload(token: String, bucket: String, key: String): ResponseEntity<ByteArray>
    suspend fun adminUpload(token: String, bucket: String, key: String, file: FilePart,isPublic : Boolean? = false)
    suspend fun adminDelete(token: String, bucket: String, key: String)
}
