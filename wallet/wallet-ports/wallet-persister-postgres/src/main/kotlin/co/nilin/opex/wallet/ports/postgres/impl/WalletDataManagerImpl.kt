package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.inout.WalletData
import co.nilin.opex.wallet.core.inout.WalletTotal
import co.nilin.opex.wallet.core.inout.WalletType
import co.nilin.opex.wallet.core.spi.WalletDataManager
import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepositoryV2
import co.nilin.opex.wallet.ports.postgres.dao.WalletRepository
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

@Component
class WalletDataManagerImpl(private val walletRepository: WalletRepository,
        private val currencyRepositoryV2: CurrencyRepositoryV2) : WalletDataManager {

    override suspend fun findWalletDataByCriteria(
        uuid: String?,
        walletType: WalletType?,
        currency: String?,
        excludeSystem: Boolean,
        limit: Int,
        offset: Int
    ): List<WalletData> {
        val currency=currencyRepositoryV2.fetchCurrency(symbol = currency)?.awaitFirstOrNull()
        return (if (!excludeSystem) walletRepository.findWalletDataByCriteria(
            uuid,
            walletType?.name?.lowercase(),
            currency?.symbol,
            limit,
            offset
        ) else
            walletRepository.findWalletDataByCriteriaExcludeSystem(
                uuid,
                walletType?.name?.lowercase(),
                currency?.symbol,
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