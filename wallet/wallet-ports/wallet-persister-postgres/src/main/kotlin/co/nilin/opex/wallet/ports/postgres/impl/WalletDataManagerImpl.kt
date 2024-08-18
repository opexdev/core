package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.inout.WalletData
import co.nilin.opex.wallet.core.inout.WalletTotal
import co.nilin.opex.wallet.core.model.WalletType
import co.nilin.opex.wallet.core.spi.WalletDataManager
import co.nilin.opex.wallet.ports.postgres.dao.WalletRepository
import kotlinx.coroutines.reactive.awaitFirstOrElse
import org.springframework.stereotype.Component

@Component
class WalletDataManagerImpl(private val walletRepository: WalletRepository) : WalletDataManager {

    override suspend fun findWalletDataByCriteria(
        uuid: String?,
        walletType: WalletType?,
        currency: String?,
        excludeSystem: Boolean,
        limit: Int,
        offset: Int
    ): List<WalletData> {
        return (if (!excludeSystem) walletRepository.findWalletDataByCriteria(
            uuid,
            walletType,
            currency,
            limit,
            offset
        ) else
            walletRepository.findWalletDataByCriteriaExcludeSystem(
                uuid,
                walletType,
                currency,
                limit,
                offset
            )).collectList().awaitFirstOrElse { emptyList() }
    }

    override suspend fun findSystemWalletsTotal(): List<WalletTotal> {
        return walletRepository.findSystemWalletsTotal().collectList().awaitFirstOrElse { emptyList() }
    }

    override suspend fun findUserWalletsTotal(): List<WalletTotal> {
        return walletRepository.findUserWalletsTotal().collectList().awaitFirstOrElse { emptyList() }
    }
}