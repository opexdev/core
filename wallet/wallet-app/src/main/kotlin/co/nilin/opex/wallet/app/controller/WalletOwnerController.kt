package co.nilin.opex.wallet.app.controller


import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.app.dto.OwnerLimitsResponse
import co.nilin.opex.wallet.app.dto.WalletData
import co.nilin.opex.wallet.app.service.CurrentUserProvider
import co.nilin.opex.wallet.app.utils.BalanceParser
import co.nilin.opex.wallet.core.spi.WalletManager
import co.nilin.opex.wallet.core.spi.WalletOwnerManager
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/owner")
class WalletOwnerController(
        private val walletOwnerManager: WalletOwnerManager,
        private val walletManager: WalletManager,
        private val environment: Environment,
        private val currentUserProvider: CurrentUserProvider
) {

    @GetMapping("/{uuid}/wallets")
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
    suspend fun getAllWallets(@PathVariable uuid: String): List<WalletData> {
        val owner = walletOwnerManager.findWalletOwner(uuid) ?: run {
            if (currentUserProvider.getCurrentUser()?.uuid.equals(uuid) && environment.activeProfiles.contains("otc"))
                walletOwnerManager.createWalletOwner(uuid, currentUserProvider.getCurrentUser()?.fullName
                        ?: currentUserProvider.getCurrentUser()?.mobile ?: "not set", "")
            throw OpexError.WalletOwnerNotFound.exception()
        }
        val wallets = walletManager.findWalletsByOwner(owner)
        return BalanceParser.parse(wallets)
    }

    @GetMapping("/{uuid}/wallets/{symbol}")
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
    suspend fun getWallet(@PathVariable uuid: String, @PathVariable symbol: String): WalletData {
        val owner = walletOwnerManager.findWalletOwner(uuid) ?: throw OpexError.WalletOwnerNotFound.exception()
        val wallets = walletManager.findWalletByOwnerAndSymbol(owner, symbol)
        return BalanceParser.parseSingleCurrency(wallets) ?: throw OpexError.WalletNotFound.exception()
    }

    @GetMapping("/{uuid}/limits")
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
    suspend fun getWalletOwnerLimits(@PathVariable uuid: String): OwnerLimitsResponse {
        val owner = walletOwnerManager.findWalletOwner(uuid) ?: throw OpexError.WalletOwnerNotFound.exception()
        return OwnerLimitsResponse(owner.isTradeAllowed, owner.isWithdrawAllowed, owner.isDepositAllowed)
    }
}