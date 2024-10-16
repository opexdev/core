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
import java.util.*

//todo This service should be use storage module as soon as possible!
@Service
class DocumentService() {
    val rootDir = "/Documents"
    suspend fun createOrUpdateDocument( content: FilePart): String? {
        var extention= content.filename().split(".").last()
        val name = generateStoragePath()
        val path = Paths.get("").resolve("$rootDir/$name.$extention").toString()
        val p = Paths.get(path)
        Files.createDirectories(p.parent)
        content.transferTo(p).awaitFirstOrNull()
        return name
    }

    fun fetchDocument(uuid: String): DocumentResponse {
        val path = Paths.get("").resolve("$rootDir/$uuid").toString()
        if (!Files.exists(Paths.get(path)))
            throw OpexError.NotFound.exception()
        return DocumentResponse(ResourceUtils.getFile(path).readText())
    }

    fun deleteDocument(uuid: String): ResponseEntity<Boolean> {
        val path = Paths.get("").resolve("$rootDir/$uuid").toString()
        if (!Files.exists(Paths.get(path)))
            throw OpexError.NotFound.exception()
        return ResponseEntity.ok().body(ResourceUtils.getFile(path).delete())
    }

    private fun generateStoragePath(): String {
        var uuid= UUID.randomUUID().toString()
        return uuid
    }


}