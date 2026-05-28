package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.spi.StorageProxy
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/opex/v1/storage")
@Tag(name = "Storage", description = "Public storage download operations.")
class StorageController(
    private val storageProxy: StorageProxy
) {
    @GetMapping
    @Operation(
        summary = "Download",
        description = """GET /opex/v1/storage.
Behavior: Public file download for the requested bucket/key. Returns binary file bytes.
Security: Public endpoint. No Bearer token is required.
""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/octet-stream",
                    schema = Schema(type = "string", format = "binary")
                )]
            )
        ]
    )
    suspend fun download(
        @Parameter(name = "bucket", description = "Storage bucket name.", required = true)
        @RequestParam("bucket") bucket: String,
        @Parameter(name = "key", description = "Storage object key.", required = true)
        @RequestParam("key") key: String
    ): ResponseEntity<ByteArray> {
        return storageProxy.publicDownload(bucket, key)
    }

}
