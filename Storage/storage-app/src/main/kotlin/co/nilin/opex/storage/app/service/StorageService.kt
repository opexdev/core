package co.nilin.opex.storage.app.service

import org.springframework.http.codec.multipart.FilePart
import java.io.FileInputStream

interface StorageService {
    suspend fun store(path: String, file: FilePart)
    suspend fun exists(filename: String): Boolean
    suspend fun load(filename: String): FileInputStream
    suspend fun delete(filename: String)
    suspend fun deleteAll(folderName: String)
}
