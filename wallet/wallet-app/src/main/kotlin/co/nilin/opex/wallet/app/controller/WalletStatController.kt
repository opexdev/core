package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.core.inout.WalletData
import co.nilin.opex.wallet.core.inout.WalletDataResponse
import co.nilin.opex.wallet.core.inout.WalletTotal
import co.nilin.opex.wallet.core.model.TotalAssetsSnapshot
import co.nilin.opex.wallet.core.model.WalletType
import co.nilin.opex.wallet.core.spi.TotalAssetsSnapshotManager
import co.nilin.opex.wallet.core.spi.WalletDataManager
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/stats")
class WalletStatController(
    private val walletDataManager: WalletDataManager,
    private val totalAssetsSnapshotManager: TotalAssetsSnapshotManager
) {

    @GetMapping("/wallets")
    suspend fun walletData(
        @RequestParam(required = false) uuid: String?,
        @RequestParam(required = false) walletType: WalletType?,
        @RequestParam(required = false) currency: String?,
        @RequestParam(required = false, defaultValue = "false") excludeSystem: Boolean,
        @RequestParam limit: Int,
        @RequestParam offset: Int
    ): List<WalletData> {
        return walletDataManager.findWalletDataByCriteria(uuid, walletType, currency, excludeSystem, limit, offset)
    }

    @GetMapping("/v2/wallets")
    suspend fun walletData(
        @RequestParam(required = false) uuid: String?,
        @RequestParam(required = false) currency: String?,
        @RequestParam(required = false, defaultValue = "false") excludeSystem: Boolean,
        @RequestParam limit: Int,
        @RequestParam offset: Int
    ): List<WalletDataResponse> {
        return walletDataManager.findWalletDataByCriteria(uuid, currency, excludeSystem, limit, offset)
    }

    @GetMapping("/wallets/system/total")
    suspend fun systemWalletTotal(): List<WalletTotal> {
        return walletDataManager.findSystemWalletsTotal()
    }

    @GetMapping("/wallets/user/total")
    suspend fun userWalletTotal(): List<WalletTotal>? {
        return walletDataManager.findUserWalletsTotal()
    }

    @GetMapping("/total-assets/{uuid}")
    suspend fun getWalletTotalAssetsSnapshot(
        @PathVariable uuid: String,
    ): TotalAssetsSnapshot? {
        return totalAssetsSnapshotManager.getUserLastSnapshot(uuid)
    }
}