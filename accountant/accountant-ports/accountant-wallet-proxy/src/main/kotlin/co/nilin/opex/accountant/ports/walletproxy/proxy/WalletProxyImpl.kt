package co.nilin.opex.accountant.ports.walletproxy.proxy

import co.nilin.opex.accountant.core.model.CurrencyPrice
import co.nilin.opex.accountant.core.model.TotalAssetsSnapshot
import co.nilin.opex.accountant.core.model.WalletType
import co.nilin.opex.accountant.core.spi.WalletProxy
import co.nilin.opex.accountant.ports.walletproxy.data.BooleanResponse
import co.nilin.opex.accountant.ports.walletproxy.data.TransferResult
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.math.BigDecimal

@Component
class WalletProxyImpl(
    private val webClient: WebClient,
    @Value("\${app.wallet.url}")
    private val walletBaseUrl: String
) : WalletProxy {

    data class TransferBody(
        val description: String?,
        val transferRef: String?,
        val transferCategory: String
    )

    override suspend fun transfer(
        symbol: String,
        senderWalletType: WalletType,
        senderUuid: String,
        receiverWalletType: WalletType,
        receiverUuid: String,
        amount: BigDecimal,
        description: String?,
        transferRef: String?,
        transferCategory: String
    ) {
        webClient.post()
            .uri("$walletBaseUrl/v2/transfer/${amount}_$symbol/from/${senderUuid}_$senderWalletType/to/${receiverUuid}_$receiverWalletType")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TransferBody(description, transferRef, transferCategory))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<TransferResult>()
            .awaitFirst()
    }

    override suspend fun canFulfil(symbol: String, walletType: WalletType, uuid: String, amount: BigDecimal): Boolean {
        return webClient.get()
            .uri("$walletBaseUrl/inquiry/$uuid/wallet_type/$walletType/can_withdraw/${amount}_$symbol")
            .header("Content-Type", "application/json")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<BooleanResponse>()
            .awaitFirst()
            .result
    }

    override suspend fun getUserTotalAssets(
        uuid: String,
    ): TotalAssetsSnapshot? {
        return webClient.get()
            .uri("$walletBaseUrl/stats/total-assets/$uuid")
            .header("Content-Type", "application/json")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<TotalAssetsSnapshot>()
            .awaitFirstOrNull()
    }

    override suspend fun getPrices(quote: String): List<CurrencyPrice> {
        return webClient.get()
            .uri("$walletBaseUrl/otc/currency/price?unit=$quote")
            .header("Content-Type", "application/json")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<List<CurrencyPrice>>()
            .awaitFirst()
    }
}