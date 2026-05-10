package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.spi.StorageProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/opex/v1/admin/storage")
@Tag(name = "Storage Admin", description = "Admin operations for storage service")
class StorageAdminController(
    private val storageProxy: StorageProxy,
    @Value("\${app.base.url}")
    private val appBaseUrl: String
) {
    @GetMapping
    @Operation(
        summary = "Admin: download file",
        description = "Download a file by bucket and key.",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(name = "bucket", `in` = ParameterIn.QUERY, required = true, schema = Schema(type = "string")),
            Parameter(name = "key", `in` = ParameterIn.QUERY, required = true, schema = Schema(type = "string"))
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [Content(mediaType = "application/octet-stream", schema = Schema(type = "string", format = "binary"))]
            )
        ]
    )
    suspend fun download(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestParam("bucket") bucket: String,
        @RequestParam("key") key: String,
    ): ResponseEntity<ByteArray> {
        return storageProxy.adminDownload(securityContext.jwtAuthentication().tokenValue(), bucket, key)
    }

    @PostMapping
    @Operation(
        summary = "Admin: upload file",
        description = "Upload a file to a bucket with a specific key.",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(name = "bucket", `in` = ParameterIn.QUERY, required = true, schema = Schema(type = "string")),
            Parameter(name = "key", `in` = ParameterIn.QUERY, required = true, schema = Schema(type = "string")),
            Parameter(name = "isPublic", `in` = ParameterIn.QUERY, required = false, schema = Schema(type = "boolean"))
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [Content(mediaType = "text/plain", schema = Schema(type = "string"))]
            )
        ]
    )
    suspend fun upload(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestParam("bucket") bucket: String,
        @RequestParam("key") key: String,
        @RequestPart("file") file: FilePart,
        @RequestParam("isPublic") isPublic: Boolean? = false,
    ): String {
        storageProxy.adminUpload(securityContext.jwtAuthentication().tokenValue(), bucket, key, file, isPublic)
        return "$appBaseUrl/opex/v1/storage?bucket=$bucket&key=$key"
    }

    @DeleteMapping
    @Operation(
        summary = "Admin: delete file",
        description = "Delete a file by bucket and key.",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(name = "bucket", `in` = ParameterIn.QUERY, required = true, schema = Schema(type = "string")),
            Parameter(name = "key", `in` = ParameterIn.QUERY, required = true, schema = Schema(type = "string"))
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "Deleted")
        ]
    )
    suspend fun delete(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestParam("bucket") bucket: String,
        @RequestParam("key") key: String,
    ) {
        storageProxy.adminDelete(securityContext.jwtAuthentication().tokenValue(), bucket, key)
    }
}