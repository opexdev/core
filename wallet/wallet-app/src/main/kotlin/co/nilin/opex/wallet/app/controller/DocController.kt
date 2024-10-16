package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.DocumentResponse
import co.nilin.opex.wallet.app.service.DocumentService
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono


@RequestMapping("/doc")
@RestController
class DocController(private val documentService: DocumentService) {
    @PostMapping("/{uuid}")
    suspend fun createOrUpdateDocument(
        @RequestPart("data") data: Mono<FilePart>
    ): ResponseEntity<String> {
        return ResponseEntity.ok(
            documentService.createOrUpdateDocument(
                data?.awaitFirst(),
            )
        )
    }


    @GetMapping("/{uuid}")
    fun getDocument(
        @RequestParam("language") language: String?,
        @PathVariable("uuid") docUuid: String,
        ): DocumentResponse? {
        return documentService.fetchDocument(language ?: "en")

    }

    @DeleteMapping("/{uuid}")
    fun deleteDocument(
        @RequestParam("language") language: String?,
        @PathVariable("uuid") docUuid: String,

        ): ResponseEntity<Boolean>? {
        return documentService.deleteDocument( docUuid)

    }

}