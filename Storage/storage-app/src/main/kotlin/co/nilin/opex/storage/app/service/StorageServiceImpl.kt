package co.nilin.opex.storage.app.service

import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

@Service
class StorageServiceImpl(private val resourceLoader: ResourceLoader) : StorageService {
    override suspend fun store(path: String, file: MultipartFile) {
        file.transferTo(Paths.get(path))
    }

    override suspend fun loadAsResource(filename: String): Resource {
        return resourceLoader.getResource(filename)
    }

    override suspend fun delete(filename: String) {
        Files.deleteIfExists(Paths.get(filename))
    }

    override suspend fun deleteAll(folderName: String) {
        File(folderName).deleteRecursively()
    }
}
