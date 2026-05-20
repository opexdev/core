package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.spi.StorageProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType


@Schema(name = "StorageUploadMultipartRequest")
data class StorageUploadMultipartRequest(

    @field:Schema(
        description = "File to upload.",
        type = "string",
        format = "binary"
    )
    val file: String? = null
)

@RestController
@RequestMapping("/opex/v1/admin/storage")
@Tag(name = "Storage Admin", description = "Admin storage upload, download, and delete operations.\n\nAllowed values:\n- isPublic: true, false.")
class StorageAdminController(
    private val storageProxy: StorageProxy,
    @Value("\${app.base.url}")
    private val appBaseUrl: String
) {
    @GetMapping
    @Operation(
        summary = "Download",
        description = """GET /opex/v1/admin/storage.
Behavior: Returns binary file bytes for the requested bucket/key.
Security: Bearer admin-token required. Required authority: ROLE_admin.
""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/octet-stream", schema = Schema(type = "string", format = "binary"))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun download(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "bucket", description = "Storage bucket name.", required = true)
        @RequestParam("bucket") bucket: String,
        @Parameter(name = "key", description = "Storage object key.", required = true)
        @RequestParam("key") key: String
    ): ResponseEntity<ByteArray> {
        return storageProxy.adminDownload(securityContext.jwtAuthentication().tokenValue(), bucket, key)
    }

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "Upload",
        description = """POST /opex/v1/admin/storage.
Behavior: Multipart upload. `file` is required. `isPublic` defaults to false when omitted.
Security: Bearer admin-token required. Required authority: ROLE_admin.

Validation: Send multipart/form-data with a `file` part. `bucket` and `key` identify the target object.
Allowed values:
- isPublic: true, false.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [
                Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = Schema(implementation = StorageUploadMultipartRequest::class)
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [
                    Content(
                        mediaType = "text/plain",
                        schema = Schema(type = "string")
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: ROLE_admin. No response body.",
                content = [Content()]
            )
        ]
    )

    suspend fun upload(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,

        @Parameter(
            name = "bucket",
            description = "Storage bucket name.",
            required = true
        )
        @RequestParam("bucket")
        bucket: String,

        @Parameter(
            name = "key",
            description = "Storage object key.",
            required = true
        )
        @RequestParam("key")
        key: String,

        @Parameter(
            name = "file",
            description = "File to upload.",
            required = true,
            schema = Schema(type = "string", format = "binary")
        )
        @RequestPart("file")
        file: FilePart,

        @Parameter(
            name = "isPublic",
            description = "Whether the uploaded object should be publicly accessible.",
            required = false,
            schema = Schema(type = "boolean", defaultValue = "false")
        )
        @RequestParam("isPublic", required = false)
        isPublic: Boolean? = false
    ): String {
        storageProxy.adminUpload(
            securityContext.jwtAuthentication().tokenValue(),
            bucket,
            key,
            file,
            isPublic
        )
        return "$appBaseUrl/opex/v1/storage?bucket=$bucket&key=$key"
    }

    @DeleteMapping
    @Operation(
        summary = "Delete",
        description = """DELETE /opex/v1/admin/storage.
Behavior: Deletes the object identified by bucket/key. Response has no body.
Security: Bearer admin-token required. Required authority: ROLE_admin.
""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "No response body.", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun delete(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "bucket", description = "Storage bucket name.", required = true)
        @RequestParam("bucket") bucket: String,
        @Parameter(name = "key", description = "Storage object key.", required = true)
        @RequestParam("key") key: String
    ) {
        storageProxy.adminDelete(securityContext.jwtAuthentication().tokenValue(), bucket, key)
    }
}
