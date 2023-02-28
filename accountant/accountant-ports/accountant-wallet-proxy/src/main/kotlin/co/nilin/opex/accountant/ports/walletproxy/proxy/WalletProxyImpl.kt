package co.nilin.opex.accountant.ports.walletproxy.proxy

import co.nilin.opex.accountant.core.inout.TransferRequest
import co.nilin.opex.accountant.core.spi.WalletProxy
import co.nilin.opex.accountant.ports.walletproxy.data.BooleanResponse
import co.nilin.opex.accountant.ports.walletproxy.data.TransferResult
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.body
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Component
class WalletProxyImpl(
    @Value("\${app.wallet.url}") val walletBaseUrl: String,
    val webClient: WebClient
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
            .uri("$walletBaseUrl/transfer/${amount}_$symbol/from/${senderUuid}_$senderWalletType/to/${receiverUuid}_$receiverWalletType")
            .contentType(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<TransferResult>()
            .log()
            .awaitFirst()
    }

    override suspend fun batchTransfer(transfers: List<TransferRequest>) {
        webClient.post()
            .uri("$walletBaseUrl/transfer/batch")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(transfers))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<TransferResult>()
            .log()
            .awaitFirstOrNull()
    }

    override suspend fun canFulfil(symbol: String, walletType: String, uuid: String, amount: BigDecimal): Boolean {
        return webClient.get()
            .uri("$walletBaseUrl/$uuid/wallet_type/$walletType/can_withdraw/${amount}_$symbol")
            .header("Content-Type", "application/json")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<BooleanResponse>()
            .log()
            .awaitFirst()
            .result
    }
}