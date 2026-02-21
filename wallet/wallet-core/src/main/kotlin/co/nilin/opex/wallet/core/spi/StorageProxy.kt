package co.nilin.opex.wallet.core.spi

import java.io.File

interface StorageProxy {
    suspend fun systemUploadFile(bucket: String, key: String, file: File, isPublic: Boolean? = false)
}
