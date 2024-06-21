package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.CurrencyDto
import co.nilin.opex.wallet.app.dto.DocumentResponse
import co.nilin.opex.wallet.app.service.DocumentService
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Mono

@RequestMapping("/currency")
@RestController
class CurrencyDocController(private val documentService: DocumentService) {
    @PostMapping("/{uuid}/doc")
    suspend fun createOrUpdateDocument(@RequestParam("language") language: String,
                                       @PathVariable("uuid") assetUuid: String,
                                       @RequestPart("data") data: Mono<FilePart>): ResponseEntity<Void> {
        return ResponseEntity.ok(documentService.createOrUpdateDocument(language, data?.awaitFirst(), assetUuid))
    }


    @GetMapping("/{uuid}/doc")
    fun getDocument(@RequestParam("language") language: String,
                    @PathVariable("uuid") assetUuid: String): DocumentResponse? {
        return documentService.fetchDocument(language, assetUuid)

    }

    @DeleteMapping("/{uuid}/doc")
    fun deleteDocument(@RequestParam("language") language: String,
                       @PathVariable("uuid") assetUuid: String): ResponseEntity<Boolean>? {
        return documentService.deleteDocument(language, assetUuid)

    }

}