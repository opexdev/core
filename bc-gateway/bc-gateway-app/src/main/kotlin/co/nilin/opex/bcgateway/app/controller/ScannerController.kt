package co.nilin.opex.bcgateway.app.controller

import co.nilin.opex.bcgateway.core.api.WalletSyncService
import co.nilin.opex.bcgateway.core.model.Transfer
import co.nilin.opex.bcgateway.core.model.Wallet
import co.nilin.opex.common.utils.SignVerifier
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.core.io.ResourceLoader
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

data class WebhookBody(
    val txId: String,
    val address: String,
    val chain: String,
    val amount: BigDecimal,
    val memo: String?,
    val isToken: Boolean,
    val tokenAddress: String?,
    val id: String?,
    val date: Long
)

@RestController
@RequestMapping("/scanner")
class ScannerController(
    private val resourceLoader: ResourceLoader,
    private val mapper: ObjectMapper,
    private val service: WalletSyncService
) {

    private val logger = LoggerFactory.getLogger(ScannerController::class.java)

    @PostMapping("/webhook")
    suspend fun webhook(@RequestHeader("X-Signature") sign: String, @RequestBody body: WebhookBody) {
        val publicKeyRawStr = resourceLoader.getResource("classpath:scanner-public.pem").inputStream
            .readAllBytes()
            .toString(Charsets.UTF_8)
        SignVerifier().verify("SHA256withRSA", publicKeyRawStr, mapper.writeValueAsString(body), sign)
        logger.info("Webhook received for address ${body.address}, amount ${body.amount}")
        service.sendTransfer(with(body) { Transfer(txId, Wallet(address, memo), isToken, amount, chain, tokenAddress) })
    }


}