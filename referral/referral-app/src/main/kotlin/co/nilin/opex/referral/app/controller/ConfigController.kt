package co.nilin.opex.referral.app.controller

import co.nilin.opex.referral.core.spi.ConfigHandler
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/configs")
class ConfigController(
    private val configHandler: ConfigHandler
) {
    data class ConfigsBody(
        var name: String,
        var referralCommissionReward: BigDecimal,
        var paymentCurrency: String,
        var minPaymentAmount: BigDecimal,
        var paymentWindowSeconds: Int,
        var maxReferralCodePerUser: Int
    )

    @ApiOperation(value = "Get referral configs", notes = "Get referral configs.")
    @ApiResponse(
        message = "OK",
        code = 200,
        response = ConfigsBody::class,
        examples = Example(
            ExampleProperty(
                mediaType = "application/json",
                value = """
{ 
    "name": "default",
    "referralCommissionReward": 0.3,
    "paymentCurrency": "usdt",
    "minPaymentAmount": 0,
    "paymentWindowSeconds": 604800,
    "maxReferralCodePerUser": 20
}
                """,
            )
        )
    )
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getReferralCodeByCode(): ConfigsBody {
        return configHandler.findConfig("default")?.let {
            ConfigsBody(
                it.name,
                it.referralCommissionReward,
                it.paymentCurrency,
                it.minPaymentAmount,
                it.paymentWindowSeconds,
                it.maxReferralCodePerUser
            )
        } ?: throw OpexException(OpexError.InternalServerError, "Config profile (default) not found")
    }
}
