package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.core.spi.WalletManager
import co.nilin.opex.wallet.core.spi.WalletOwnerManager
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.security.Principal

@RestController
class WalletOwnerController(
    val walletManager: WalletManager,
    val walletOwnerManager: WalletOwnerManager
) {

    data class WalletData(
        val asset: String,
        val balance: BigDecimal,
        val type: String
    )

    data class OwnerLimitsResponse(
        val canTrade: Boolean,
        val canWithdraw: Boolean,
        val canDeposit: Boolean
    )

    @GetMapping("/owner/wallet/all")
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "{ }",
                mediaType = "application/json"
            )
        )
    )
    suspend fun getAllWallets(principal: Principal): List<WalletData> {
        val owner = walletOwnerManager.findWalletOwner(principal.name)
        if (owner != null) {
            val wallets = walletManager.findWalletsByOwner(owner)
            return wallets.map {
                WalletData(it.currency().getSymbol(), it.balance().amount, it.type())
            }
        }
        return arrayListOf()
    }

    @GetMapping("/owner/limits")
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "{ }",
                mediaType = "application/json"
            )
        )
    )
    suspend fun getWalletOwnerLimits(principal: Principal): OwnerLimitsResponse {
        val owner = walletOwnerManager.findWalletOwner(principal.name)
        return if (owner != null)
            OwnerLimitsResponse(owner.isTradeAllowed(), owner.isWithdrawAllowed(), owner.isDepositAllowed())
        else
            OwnerLimitsResponse(canTrade = false, canWithdraw = false, canDeposit = false)
    }
}