package co.nilin.opex.storage.app.service

import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile

interface StorageService {
    suspend fun store(path: String, file: MultipartFile)
    suspend fun loadAsResource(filename: String): Resource
    suspend fun delete(filename: String)
    suspend fun deleteAll(folderName: String)
}
