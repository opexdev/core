package co.nilin.opex.storage.app.service

import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile

interface StorageService {
    fun store(path: String, file: MultipartFile)
    fun loadAsResource(filename: String): Resource
    fun delete(filename: String)
    fun deleteAll(folderName: String)
}
