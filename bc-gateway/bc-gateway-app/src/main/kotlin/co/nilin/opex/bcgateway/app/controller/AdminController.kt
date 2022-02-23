package co.nilin.opex.bcgateway.app.controller

import co.nilin.opex.bcgateway.app.dto.AddChainRequest
import co.nilin.opex.bcgateway.app.dto.AddressTypeRequest
import co.nilin.opex.bcgateway.app.dto.TokenRequest
import co.nilin.opex.bcgateway.app.service.AdminService
import co.nilin.opex.bcgateway.core.model.AddressType
import co.nilin.opex.bcgateway.core.model.Chain
import co.nilin.opex.bcgateway.core.model.CurrencyImplementation
import co.nilin.opex.bcgateway.core.spi.AddressTypeHandler
import co.nilin.opex.bcgateway.core.spi.ChainLoader
import co.nilin.opex.bcgateway.core.spi.CurrencyHandler
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin")
class AdminController(
    private val service: AdminService,
    private val chainLoader: ChainLoader,
    private val currencyHandler: CurrencyHandler,
    private val addressTypeHandler: AddressTypeHandler
) {

    @GetMapping("/chain")
    suspend fun getChains(): List<Chain> {
        return chainLoader.fetchAllChains()
    }

    @PostMapping("/chain")
    suspend fun addChain(@RequestBody body: AddChainRequest) {
        if (!body.isValid())
            throw OpexException(OpexError.InvalidRequestBody)
        service.addChain(body)
    }

    @GetMapping("/address/type")
    suspend fun getAddressTypes(): List<AddressType> {
        return addressTypeHandler.fetchAll()
    }

    @PostMapping("/address/type")
    suspend fun addAddressType(@RequestBody body: AddressTypeRequest) {
        if (body.name.isNullOrEmpty() || body.addressRegex.isNullOrEmpty())
            throw OpexException(OpexError.InvalidRequestBody)
        service.addAddressType(body.name, body.addressRegex, body.memoRegex)
    }

    @GetMapping("/token")
    suspend fun getCurrencyImplementation(): List<CurrencyImplementation> {
        return currencyHandler.fetchAllImplementations()
    }

    @PostMapping("/token")
    suspend fun addCurrencyImplementation(@RequestBody body: TokenRequest): CurrencyImplementation {
        val ex = OpexException(OpexError.InvalidRequestBody)
        with(body) {
            if (symbol.isNullOrEmpty() || chain.isNullOrEmpty()) throw ex
            if (isToken && (tokenName.isNullOrEmpty() || tokenAddress.isNullOrEmpty())) throw ex
            if (withdrawFee < 0 || minimumWithdraw < 0 || decimal < 1) throw ex
        }

        return service.addToken(body)
    }

    @PutMapping("/token/{symbol}_{chain}/withdraw")
    suspend fun changeWithdrawStatus(
        @PathVariable symbol: String,
        @PathVariable chain: String,
        @RequestParam("enabled") status: Boolean
    ) {
        service.changeTokenWithdrawStatus(symbol, chain, status)
    }

}