package co.nilin.opex.wallet.app.service

import co.nilin.opex.wallet.core.model.BriefWallet
import co.nilin.opex.wallet.core.model.WalletOwner
import co.nilin.opex.wallet.core.spi.StorageProxy
import co.nilin.opex.wallet.core.spi.WalletManager
import co.nilin.opex.wallet.core.spi.WalletOwnerManager
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.time.Instant
import com.google.api.services.drive.model.File as GoogleFile

@Service
@Profile("!test")
class BackupService(
    private val walletManager: WalletManager,
    private val walletOwnerManager: WalletOwnerManager,
    private val storageProxy: StorageProxy,
    @Value("\${app.backup.storage.enabled}") private val storageEnabled: Boolean,
    @Value("\${app.backup.google-drive.enabled}") private val driveEnabled: Boolean,
    @Value("\${app.backup.storage.folder}") private val storageFolderId: String,
    @Value("\${app.backup.google-drive.folder}") private val driveFolderId: String
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val mapper = ObjectMapper().findAndRegisterModules()

    data class OwnerAndWallets(
        val owner: WalletOwner,
        val wallets: List<BriefWallet>
    )

    @Scheduled(initialDelay = 60_000, fixedDelay = 10 * 60 * 1000)
    fun backup() = runBlocking {
        if (!storageEnabled && !driveEnabled) return@runBlocking

        val fileName = "wallets-${Instant.now().toEpochMilli()}.json"
        logger.info("Starting wallet backup: $fileName")

        try {
            val tempFile = writeBackupToTempFile()

            if (storageEnabled) {
                storageProxy.systemUploadFile(
                    bucket = storageFolderId,
                    key = fileName,
                    file = tempFile,
                )
            }

            if (driveEnabled) {
                uploadToGoogleDrive(tempFile, fileName)
            }

            tempFile.delete()
        } catch (e: Exception) {
            logger.error("Wallet backup failed", e)
        }
    }

    private suspend fun writeBackupToTempFile(): File =
        withContext(Dispatchers.IO) {
            val file = Files.createTempFile("wallets-", ".json").toFile()
            val data = walletOwnerManager.findAllWalletOwners().map { owner ->
                OwnerAndWallets(
                    owner,
                    walletManager.findAllWalletsBriefNotZero(owner.id!!)
                )
            }
            mapper.writeValue(file, data)
            file
        }

    private fun uploadToGoogleDrive(file: File, fileName: String) {
        logger.info("Uploading backup to Google Drive")

        val credentials = GoogleCredentials
            .fromStream(File("/drive-key.json").inputStream())
            .createScoped(DriveScopes.DRIVE)

        val drive = Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            HttpCredentialsAdapter(credentials)
        )
            .setApplicationName("Wallet backup")
            .build()

        val metadata = GoogleFile().apply {
            name = fileName
            parents = listOf(driveFolderId)
        }

        drive.files().create(
            metadata,
            FileContent(MediaType.APPLICATION_JSON_VALUE, file)
        )
            .setFields("id,name")
            .execute()
            .also {
                logger.info("Uploaded to Drive: ${it.id}")
            }
    }
}
