package co.nilin.opex.bcgateway.app.controller

import co.nilin.opex.bcgateway.core.api.WalletSyncService
import co.nilin.opex.bcgateway.core.model.Transfer
import co.nilin.opex.bcgateway.core.model.Wallet
import co.nilin.opex.common.OpexError
import co.nilin.opex.utility.error.data.OpexException
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.core.io.ResourceLoader
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.security.PublicKey
import java.security.Signature
import java.util.*

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
    private val publicKey: PublicKey,
    private val mapper: ObjectMapper,
    private val service: WalletSyncService
) {

    private val logger = LoggerFactory.getLogger(ScannerController::class.java)

    @PostMapping("/webhook")
    suspend fun webhook(@RequestHeader("X-Signature") sign: String, @RequestBody body: WebhookBody) {
        verifySignature(sign, body)
        logger.info("Webhook received for address ${body.address}, amount ${body.amount}")
        service.sendTransfer(with(body) { Transfer(txId, Wallet(address, memo), isToken, amount, chain, tokenAddress) })
    }

    private fun verifySignature(sign: String, request: WebhookBody) {
        try {
            logger.info("Verifying signature for address ${request.address}")
            val reqStr = mapper.writeValueAsString(request)
            val decodedSign = Base64.getDecoder().decode(sign)
            val verifier = Signature.getInstance("SHA256withRSA").apply {
                initVerify(publicKey)
                update(reqStr.toByteArray())
            }

            if (!verifier.verify(decodedSign)) {
                logger.warn("Signature is not valid!")
                throw OpexError.Forbidden.exception()
            }
        } catch (e: OpexException) {
            throw e
        } catch (e: Exception) {
            logger.error("Unable to verify signature", e)
            throw OpexError.InternalServerError.exception()
        }
    }
}