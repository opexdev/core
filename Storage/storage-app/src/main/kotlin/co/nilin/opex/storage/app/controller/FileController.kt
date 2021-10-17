package co.nilin.opex.storage.app.controller

import co.nilin.opex.storage.app.service.StorageService
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.core.io.Resource

@RestController
class FileController(private val storageService: StorageService) {
    @PostMapping("/{uid}")
    suspend fun fileUpload(
        @PathVariable("uid") uid: String,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<String> {
        val ext = file.name.replace(Regex(".+(?=\\..+)"), "")
        if (ext !in listOf(".jpg", ".png", ".mp4", ".mov")) return ResponseEntity.badRequest()
            .body("Invalid File Format")
        storageService.store("/opex-storage/$uid/${file.name}", file)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/{uid}/{filename}")
    suspend fun fileDownload(
        @PathVariable("uid") uid: String,
        @PathVariable("filename") filename: String
    ): ResponseEntity<Resource> {
        val resource = storageService.loadAsResource("/opex-storage/$uid/$filename")
        return ResponseEntity.ok().header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + resource.filename + "\""
        ).body(resource);
    }
}
