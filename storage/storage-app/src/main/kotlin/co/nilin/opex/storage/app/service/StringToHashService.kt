package co.nilin.opex.storage.app.service

import org.springframework.stereotype.Service
import java.util.zip.CRC32

@Service
class StringToHashService {
    private val crc32 = CRC32()

    fun digest(input: String): String {
        crc32.update(input.toByteArray())
        return String.format("%02x", crc32.value)
    }
}
