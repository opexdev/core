package co.nilin.opex.storage.app.controller

import co.nilin.opex.storage.app.service.StorageService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class FileController(private val storageService: StorageService) {
    @PostMapping("/{uid}/profile")
    suspend fun fileUpload(@PathVariable("uid") uid: String, @RequestParam("file") file: MultipartFile) {
        val ext = file.name.replace(Regex(".+(?=\\..+)"), "")
        storageService.store("/$uid/profile.$ext", file)
    }
}
