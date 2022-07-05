package co.nilin.opex.wallet.app.controller

import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.wallet.app.dto.OwnerLimitsResponse
import co.nilin.opex.wallet.app.dto.WalletData
import co.nilin.opex.wallet.app.utils.BalanceParser
import co.nilin.opex.wallet.core.spi.WalletManager
import co.nilin.opex.wallet.core.spi.WalletOwnerManager
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/owner")
class WalletOwnerController(
    private val walletOwnerManager: WalletOwnerManager,
    private val walletManager: WalletManager
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
        val owner = walletOwnerManager.findWalletOwner(uuid) ?: throw OpexException(OpexError.WalletOwnerNotFound)
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
        val owner = walletOwnerManager.findWalletOwner(uuid) ?: throw OpexException(OpexError.WalletOwnerNotFound)
        val wallets = walletManager.findWalletByOwnerAndSymbol(owner, symbol)
        return BalanceParser.parseSingleCurrency(wallets) ?: throw OpexException(OpexError.WalletNotFound)
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
        val owner = walletOwnerManager.findWalletOwner(uuid) ?: throw OpexException(OpexError.WalletOwnerNotFound)
        return OwnerLimitsResponse(owner.isTradeAllowed, owner.isWithdrawAllowed, owner.isDepositAllowed)
    }
}