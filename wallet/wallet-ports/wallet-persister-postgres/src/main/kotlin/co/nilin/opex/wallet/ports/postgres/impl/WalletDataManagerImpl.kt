package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.inout.WalletData
import co.nilin.opex.wallet.core.inout.WalletType
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
        limit: Int,
        offset: Int
    ): List<WalletData> {
        return walletRepository.findWalletDataByCriteria(
            uuid,
            walletType?.name?.lowercase(),
            currency,
            limit,
            offset
        ).collectList().awaitFirstOrElse { emptyList() }
    }
}