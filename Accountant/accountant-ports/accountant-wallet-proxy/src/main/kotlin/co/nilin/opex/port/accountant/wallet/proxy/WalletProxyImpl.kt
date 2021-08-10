package co.nilin.opex.port.accountant.wallet.proxy

import co.nilin.opex.accountant.core.spi.WalletProxy
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigDecimal
import java.net.URI
import java.time.LocalDateTime

inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}
data class TransferResult(val date: LocalDateTime, val sourceBalanceBeforeAction: Amount, val sourceBalanceAfterAction: Amount, val amount: Amount)
data class Amount(val currency: Currency, val amount: BigDecimal)
data class Currency(val name: String, val symbol: String, val precision: Int)

@Component
class WalletProxyImpl(@Value("\${app.wallet.url}") val walletBaseUrl: String
,     val webClient: WebClient
): WalletProxy {
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
            .uri(URI.create("$walletBaseUrl/transfer/${amount}_${symbol}/from/${senderUuid}_${senderWalletType}/to/${receiverUuid}_${receiverWalletType}"))
            .header("Content-Type", "application/json")
            .retrieve()
            .onStatus({ t -> t.isError }, { p ->
                /*
                p.bodyToMono(typeRef<SejamResponse<Any>>()).map { t -> KycSejamException(p.statusCode().value().toString(), t.error?.errorCode.toString()
                        + "-" + t.error?.customMessage) }
                        */
                throw RuntimeException()
            })
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
            .onStatus({ t -> t.isError }, { p ->
                throw RuntimeException()
            })
            .bodyToMono(typeRef<BooleanResponse>())
            .log()
            .awaitFirst()
            .result
    }
}