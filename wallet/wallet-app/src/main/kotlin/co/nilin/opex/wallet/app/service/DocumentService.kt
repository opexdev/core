package co.nilin.opex.wallet.app.service

import co.nilin.opex.common.OpexError
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.util.ResourceUtils
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

//todo This service should be use storage module as soon as possible!
@Service
class DocumentService() {
    val rootDir = "/Documents"
    suspend fun createOrUpdateDocument(content: FilePart): ResponseEntity<String>? {
        var extention = content.filename().split(".").last()
        val name = generateStoragePath()
        val path = Paths.get("").resolve("$rootDir/$name.$extention").toString()
        val p = Paths.get(path)
        Files.createDirectories(p.parent)
        content.transferTo(p).awaitFirstOrNull()
        return ResponseEntity.ok("$name.$extention")
    }

    fun fetchDocument(file: String): ResponseEntity<Any> {
        val path = Paths.get("").resolve("$rootDir/$file").toString()
        if (!Files.exists(Paths.get(path)))
            throw OpexError.NotFound.exception()
        var file = ResourceUtils.getFile(path).inputStream()
        val mimeType = URLConnection.getFileNameMap().getContentTypeFor(path)
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(mimeType)).body(file.readBytes())

    }

    fun deleteDocument(file: String): ResponseEntity<Boolean> {
        val path = Paths.get("").resolve("$rootDir/$file").toString()
        if (!Files.exists(Paths.get(path)))
            throw OpexError.NotFound.exception()
        return ResponseEntity.ok().body(ResourceUtils.getFile(path).delete())
    }

    private fun generateStoragePath(): String {
        return UUID.randomUUID().toString()
    }


}