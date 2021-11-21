package co.nilin.opex.storage.app.service

import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.core.io.ResourceLoader
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.util.ResourceUtils
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Paths

@Service
class StorageServiceImpl(private val resourceLoader: ResourceLoader) : StorageService {
    override suspend fun store(path: String, file: FilePart) {
        val p = Paths.get(path)
        Files.createDirectories(p.parent)
        file.transferTo(p).awaitFirstOrNull()
    }

    override suspend fun exists(filename: String): Boolean {
        return Files.exists(Paths.get(filename))
    }

    override suspend fun load(filename: String): FileInputStream {
        return ResourceUtils.getFile(filename).inputStream()
    }

    override suspend fun delete(filename: String) {
        Files.deleteIfExists(Paths.get(filename))
    }

    override suspend fun deleteAll(folderName: String) {
        File(folderName).deleteRecursively()
    }
}
