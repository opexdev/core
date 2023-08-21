package co.nilin.opex.storage.app.controller

import co.nilin.opex.storage.app.service.StorageService
import co.nilin.opex.storage.app.service.StringToHashService
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.net.URLConnection
import java.nio.file.Paths
import java.util.*

@RestController
class FileController(
    private val storageService: StorageService,
    private val stringToHashService: StringToHashService,
    @Value("\${app.root-dir}") private val rootDir: String
) {
    data class FileUploadResponse(val path: String)
    private val logger = LoggerFactory.getLogger(FileController::class.java)


   private  suspend fun upload(uid: String, file: FilePart?, nameWithoutExtension: String? = null): FileUploadResponse {
        if (file == null) throw OpexException(OpexError.BadRequest, "File Not Provided")
        val filename = file.filename()
        val ext = filename.replace(Regex(".+(?<=\\.)"), "")
        if (ext.toLowerCase() !in listOf("jpg", "jpeg", "png", "mp4", "mov"))
            throw OpexException(OpexError.BadRequest, "Invalid File Format")
        val uri = if (nameWithoutExtension == null) {
            "$uid/$filename"
        } else {
            "$uid/$nameWithoutExtension.$ext"
        }
        val path = Paths.get("").resolve("$rootDir/$uri").toString()
        storageService.store(path, file)
        return FileUploadResponse("/$uri")
    }

    private suspend fun download(uid: String, filename: String? = null): ResponseEntity<ByteArray> {
        val path = Paths.get("").resolve("$rootDir/$uid/$filename")
        if (!storageService.exists(path.toString())) throw OpexException(OpexError.NotFound)
        val file = storageService.load(path.toString())
        val mimeType = URLConnection.getFileNameMap().getContentTypeFor(path.fileName.toString())
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(mimeType)).body(file.readBytes())
    }

    @PostMapping("/{uid}")
    suspend fun fileUploadPost(
        @PathVariable("uid") uid: String,
        @RequestPart("file") file: Mono<FilePart>,
        @CurrentSecurityContext securityContext: SecurityContext
    ): FileUploadResponse {
        //todo reset authentication filter
        if (securityContext.authentication.name != uid) throw OpexException(OpexError.UnAuthorized)
        return upload(uid, file.awaitFirstOrNull(), stringToHashService.digest(UUID.randomUUID().toString()))
    }

    @GetMapping("/{uid}/{filename}")
    @ResponseBody
    suspend fun download(
        @PathVariable("uid") uid: String,
        @PathVariable("filename") filename: String,
        @CurrentSecurityContext securityContext: SecurityContext
    ): ResponseEntity<ByteArray> {
        if (securityContext.authentication.name != uid) throw OpexException(OpexError.UnAuthorized)
        return download(uid, filename)
    }



    @GetMapping("/admin/download/{uid}/{filename}")
    @ResponseBody
    suspend fun adminFileDownload(
        @PathVariable("uid") uid: String,
        @PathVariable("filename") filename: String
    ): ResponseEntity<ByteArray> {
        return download(uid, filename)
    }
}
