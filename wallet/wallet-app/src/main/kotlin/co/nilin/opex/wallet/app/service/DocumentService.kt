package co.nilin.opex.wallet.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.app.dto.DocumentResponse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.util.ResourceUtils
import java.nio.file.Files
import java.nio.file.Paths

//todo This service should be use storage module as soon as possible!
@Service
class DocumentService() {
    val rootDir = "/Documents"
    suspend fun createOrUpdateDocument(language: String, content: FilePart, uuid: String):Void? {
        val name = generateStoragePath(language, uuid)
        val path = Paths.get("").resolve("$rootDir/$name").toString()
        val p = Paths.get(path)
        Files.createDirectories(p.parent)
        return content.transferTo(p).awaitFirstOrNull()
    }

    fun fetchDocument(language: String, uuid: String): DocumentResponse {
        val name = generateStoragePath(language, uuid)
        val path = Paths.get("").resolve("$rootDir/$name").toString()
        if (!Files.exists(Paths.get(path)))
            throw OpexError.NotFound.exception()
        return DocumentResponse(ResourceUtils.getFile(path).readText())
    }

    fun deleteDocument(language: String, uuid: String): ResponseEntity<Boolean> {
        val name = generateStoragePath(language, uuid)
        val path = Paths.get("").resolve("$rootDir/$name").toString()
        if (!Files.exists(Paths.get(path)))
            throw OpexError.NotFound.exception()
        return ResponseEntity.ok().body(ResourceUtils.getFile(path).delete())
    }

    private fun generateStoragePath(language: String, uuid: String): String {
        return "${uuid}/${language}.txt"
    }


}