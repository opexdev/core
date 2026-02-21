package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.PaymentDepositRequest
import co.nilin.opex.wallet.app.dto.PaymentDepositResponse
import co.nilin.opex.wallet.app.service.DepositService
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/payment")
class PaymentGatewayController(
    val depositService: DepositService

) {

    //todo refactor
    //todo get and check gatewayUUId and terminalUUId
    @PostMapping("/internal/deposit")
    @Transactional
    suspend fun paymentDeposit(@RequestBody request: PaymentDepositRequest): PaymentDepositResponse {
        return depositService.commitPaymentDeposit(request)
    }

}