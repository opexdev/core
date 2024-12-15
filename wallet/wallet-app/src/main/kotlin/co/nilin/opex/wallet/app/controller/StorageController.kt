package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.service.DocumentService
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono


@RequestMapping("/storage")
@RestController
class StorageController(private val documentService: DocumentService) {
    @PostMapping
    suspend fun createOrUpdateDocument(
        @RequestPart("data") data: Mono<FilePart>
    ): ResponseEntity<String>? {
        return documentService.createOrUpdateDocument(
            data?.awaitFirst()
        )
    }


    @GetMapping("/{file}")
    fun getDocument(
        @PathVariable("file") file: String,
    ): ResponseEntity<Any>? {
        return documentService.fetchDocument(file)

    }

    @DeleteMapping("/{file}")
    fun deleteDocument(
        @PathVariable("file") file: String,
    ): ResponseEntity<Boolean>? {
        return documentService.deleteDocument(file)

    }

}