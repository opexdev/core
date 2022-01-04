package co.nilin.opex.referral.ports.wallet.proxy.proxy

import co.nilin.opex.referral.core.spi.WalletProxy
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigDecimal
import java.net.URI

inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

data class TransferResult(
    val date: Long,
    val sourceUuid: String,
    val sourceWalletType: String,
    val sourceBalanceBeforeAction: Amount,
    val sourceBalanceAfterAction: Amount,
    val amount: Amount,
    val destUuid: String,
    val destWalletType: String,
    val receivedAmount: Amount
)

data class Amount(val currency: Currency, val amount: BigDecimal)
data class Currency(val name: String, val symbol: String, val precision: Int)

@Component
class WalletProxyImpl(
    @Value("\${app.wallet.url}") val walletBaseUrl: String, val webClient: WebClient
) : WalletProxy {
    override suspend fun transfer(
        symbol: String,
        senderWalletType: String,
        senderUuid: String,
        receiverWalletType: String,
        receiverUuid: String,
        amount: BigDecimal,
        description: String?,
        transferRef: String?
    ) {
        webClient.post()
            .uri(URI.create("$walletBaseUrl/transfer/${amount}_${symbol}/from/${senderUuid}_${senderWalletType}/to/${receiverUuid}_${receiverWalletType}?description=$description&transferRef=$transferRef"))
            .header("Content-Type", "application/json")
            .retrieve()
            .onStatus({ t -> t.isError }, { throw RuntimeException() })
            .bodyToMono(typeRef<TransferResult>())
            .log()
            .awaitFirst()
    }

    override suspend fun canFulfil(symbol: String, walletType: String, uuid: String, amount: BigDecimal): Boolean {
        data class BooleanResponse(val result: Boolean)
        return webClient.get()
            .uri(URI.create("$walletBaseUrl/$uuid/wallet_type/${walletType}/can_withdraw/${amount}_${symbol}"))
            .header("Content-Type", "application/json")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono(typeRef<BooleanResponse>())
            .log()
            .awaitFirst()
            .result
    }
}
