package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.DepositWebhookRequest
import co.nilin.opex.api.core.inout.DepositWebhookResponse
import co.nilin.opex.api.core.inout.DepositWebhookHeaders
import co.nilin.opex.api.core.spi.WalletProxy
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/deposit")
class DepositWebhookController(
    private val walletProxy: WalletProxy
) {
    @PostMapping(
        "/webhook"
    )
    suspend fun submit(
        @RequestHeader(DepositWebhookHeaders.SIGNATURE) sign: String, @RequestBody body: DepositWebhookRequest
    ): DepositWebhookResponse {
        return walletProxy.submitDepositWebhook(
            body,
            sign
        )
    }
}


