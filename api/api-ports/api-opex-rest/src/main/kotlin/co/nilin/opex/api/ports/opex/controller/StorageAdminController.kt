package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.spi.StorageProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/opex/v1/admin/storage")
class StorageAdminController(
    private val storageProxy: StorageProxy,
) {
    @GetMapping
    suspend fun download(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestParam("bucket") bucket: String,
        @RequestParam("key") key: String,
    ): ResponseEntity<ByteArray> {
        return storageProxy.adminDownload(securityContext.jwtAuthentication().tokenValue(), bucket, key)
    }

    @PostMapping
    suspend fun upload(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestParam("bucket") bucket: String,
        @RequestParam("key") key: String,
        @RequestPart("file") file: FilePart,
        @RequestParam("isPublic") isPublic: Boolean? = false,
    ) {
        storageProxy.adminUpload(securityContext.jwtAuthentication().tokenValue(), bucket, key, file, isPublic)
    }

    @DeleteMapping
    suspend fun delete(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestParam("bucket") bucket: String,
        @RequestParam("key") key: String,
    ) {
        storageProxy.adminDelete(securityContext.jwtAuthentication().tokenValue(), bucket, key)
    }
}