package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.inout.WalletCurrencyData
import co.nilin.opex.wallet.core.inout.WalletData
import co.nilin.opex.wallet.core.inout.WalletDataResponse
import co.nilin.opex.wallet.core.inout.WalletTotal
import co.nilin.opex.wallet.core.model.WalletType
import co.nilin.opex.wallet.core.spi.WalletDataManager
import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepositoryV2
import co.nilin.opex.wallet.ports.postgres.dao.WalletRepository
import co.nilin.opex.wallet.ports.postgres.model.CurrencyModel
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

@Component
class WalletDataManagerImpl(
    private val walletRepository: WalletRepository,
    private val currencyRepositoryV2: CurrencyRepositoryV2,
    private val objectMapper: ObjectMapper
) : WalletDataManager {

    override suspend fun findWalletDataByCriteria(
        uuid: String?,
        walletType: WalletType?,
        currency: String?,
        excludeSystem: Boolean,
        limit: Int,
        offset: Int
    ): List<WalletData> {
        val currency = currencyRepositoryV2.fetchCurrency(symbol = currency)?.awaitFirstOrNull()
        return (if (!excludeSystem) walletRepository.findWalletDataByCriteria(
            uuid,
            walletType,
            currency?.symbol,
            limit,
            offset
        ) else
            walletRepository.findWalletDataByCriteriaExcludeSystem(
                uuid,
                walletType,
                currency?.symbol,
                limit,
                offset
            )).collectList().awaitFirstOrElse { emptyList() }
    }

    override suspend fun findWalletDataByCriteria(
        uuid: String?,
        currency: String?,
        excludeSystem: Boolean,
        limit: Int,
        offset: Int
    ): List<WalletDataResponse> {
        return walletRepository.findWalletDataByCriteria(
            uuid,
            currency,
            excludeSystem,
            limit,
            offset
        ).map { raw ->
            val walletsList = try {
                objectMapper.readValue(
                    raw.wallets,
                    object : TypeReference<List<WalletCurrencyData>>() {}
                )
            } catch (e: Exception) {
                emptyList<WalletCurrencyData>()
            }
            WalletDataResponse(
                uuid = raw.uuid,
                title = raw.title,
                wallets = walletsList
            )
        }.collectList().awaitFirstOrElse { emptyList() }
    }

    override suspend fun findSystemWalletsTotal(): List<WalletTotal> {
        return walletRepository.findSystemWalletsTotal().collectList().awaitFirstOrElse { emptyList() }
    }

    override suspend fun findUserWalletsTotal(): List<WalletTotal>? {
        val allCurrencies = currencyRepositoryV2.fetchSemiCurrencies()?.map(CurrencyModel::symbol)
        val allDepositedCurrency =
            walletRepository.findUserWalletsTotal().collectList().awaitFirstOrElse { emptyList() }
        return allCurrencies?.map { c ->
            WalletTotal(c, (allDepositedCurrency.filter { it.currency == c }?.firstOrNull()?.balance) ?: 0.0)
        }?.collectList()?.awaitFirstOrNull()
    }
}