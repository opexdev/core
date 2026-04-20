package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.spi.StorageProxy
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/opex/v1/storage")
class StorageController(
    private val storageProxy: StorageProxy,
) {
    @GetMapping
    suspend fun download(
        @RequestParam("bucket") bucket: String,
        @RequestParam("key") key: String,
    ): ResponseEntity<ByteArray> {
        return storageProxy.publicDownload(bucket, key)
    }

}