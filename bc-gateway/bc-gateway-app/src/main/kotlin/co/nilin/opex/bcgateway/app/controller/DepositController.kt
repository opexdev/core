package co.nilin.opex.bcgateway.app.controller

import co.nilin.opex.bcgateway.core.api.DepositService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
class DepositController(private val depositService: DepositService) {

    data class GetDepositsRequest(val refs: List<String>)

    data class DepositRequest(
        val hash: String,
        val address: String,
        val memo: String?,
        val amount: BigDecimal,
        val chain: String,
        val isToken: Boolean,
        val tokenAddress: String?
    )

    @PostMapping("/deposit/find/all")
    suspend fun getDeposits(@RequestBody request: GetDepositsRequest): List<DepositRequest> {
        return depositService.getDepositDetails(request.refs)
            .map {
                DepositRequest(
                    it.hash,
                    it.depositor,
                    it.depositorMemo,
                    it.amount,
                    it.chain,
                    it.token,
                    it.tokenAddress
                )
            }
    }

}