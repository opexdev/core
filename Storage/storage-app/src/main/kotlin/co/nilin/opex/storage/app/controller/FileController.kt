package co.nilin.opex.storage.app.controller

import co.nilin.opex.storage.app.service.StorageService
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.net.URLConnection
import java.nio.file.Paths
import kotlin.io.path.exists

@RestController
class FileController(private val storageService: StorageService) {
    data class FileUploadResponse(val path: String)

    @PostMapping("/{uid}")
    suspend fun fileUpload(
            @PathVariable("uid") uid: String,
            @RequestPart("file") file: Mono<FilePart>,
            @CurrentSecurityContext securityContext: SecurityContext
    ): Any {
        if (securityContext.authentication.name != uid) throw OpexException(OpexError.UnAuthorized)
        file.awaitFirstOrNull().apply {
            if (this == null) throw OpexException(OpexError.BadRequest, "File Not Provided")
            val ext = this.filename().replace(Regex(".+(?=\\..+)"), "")
            if (ext.toLowerCase() !in listOf(".jpg", ".jpeg", ".png", ".mp4", ".mov", ".pdf", ".svg"))
                throw OpexException(OpexError.BadRequest, "Invalid File Format")
            val path = Paths.get("").resolve("/opex-storage/$uid/${this.filename()}").toString()
            storageService.store(path, this)
            return FileUploadResponse("/$uid/${this.filename()}")
        }
    }

    @GetMapping("/{uid}/{filename}")
    @ResponseBody
    suspend fun fileDownload(
            @PathVariable("uid") uid: String,
            @PathVariable("filename") filename: String,
            @CurrentSecurityContext securityContext: SecurityContext
    ): ResponseEntity<ByteArray> {
        if (securityContext.authentication.name != uid) throw OpexException(OpexError.UnAuthorized)
        val path = Paths.get("").resolve("/opex-storage/$uid/$filename")
        if (!storageService.exists(path.toString())) throw OpexException(OpexError.NotFound)
        val file = storageService.load(path.toString())
        val mimeType = URLConnection.getFileNameMap().getContentTypeFor(path.fileName.toString())
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(mimeType)).body(file.readBytes())
    }

    @GetMapping("/admin/download/{uid}/{filename}")
    @ResponseBody
    suspend fun adminFileDownload(
            @PathVariable("uid") uid: String,
            @PathVariable("filename") filename: String
    ): ResponseEntity<ByteArray> {
        val path = Paths.get("").resolve("/opex-storage/$uid/$filename")
        if (!storageService.exists(path.toString())) throw OpexException(OpexError.NotFound)
        val file = storageService.load(path.toString())
        val mimeType = URLConnection.getFileNameMap().getContentTypeFor(path.fileName.toString())
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(mimeType)).body(file.readBytes())
    }
}
