package co.nilin.opex.bcgateway.app.controller

import co.nilin.opex.bcgateway.app.dto.*
import co.nilin.opex.bcgateway.app.service.AdminService
import co.nilin.opex.bcgateway.core.model.AddressType
import co.nilin.opex.bcgateway.core.spi.AddressTypeHandler
import co.nilin.opex.bcgateway.core.spi.ChainLoader
import co.nilin.opex.bcgateway.ports.postgres.impl.CurrencyHandlerImplV2
import co.nilin.opex.common.OpexError
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin")
class AdminController(
    private val service: AdminService,
    private val chainLoader: ChainLoader,
    private val currencyHandler: CurrencyHandlerImplV2,
    private val addressTypeHandler: AddressTypeHandler,
) {

    @GetMapping("/chain")
    suspend fun getChains(): List<ChainResponse> {
        return chainLoader.fetchAllChains().map { c -> ChainResponse(c.name, c.addressTypes.map { it.type }) }
    }

    @PostMapping("/chain")
    suspend fun addChain(@RequestBody body: AddChainRequest) {
        if (!body.isValid())
            throw OpexError.InvalidRequestBody.exception()
        service.addChain(body)
    }

    @GetMapping("/address/type")
    suspend fun getAddressTypes(): List<AddressType> {
        return addressTypeHandler.fetchAll()
    }

    @PostMapping("/address/type")
    suspend fun addAddressType(@RequestBody body: AddressTypeRequest) {
        if (body.name.isNullOrEmpty() || body.addressRegex.isNullOrEmpty())
            throw OpexError.InvalidRequestBody.exception()
        service.addAddressType(body.name, body.addressRegex, body.memoRegex)
    }

    @PostMapping("/address")
    suspend fun addAddress(@RequestBody body: AddAddressRequest) {
        service.addAddress(body.addresses, body.memos, body.addressType)
    }

    // shifted to crypto currency class!

//    //todo filter tokens?????
//    @GetMapping("/token")
//    suspend fun getCurrencyImplementation(): List<TokenResponse>? {
//        return currencyHandler.fetchCurrencyImpls()?.imps
//                ?.map {
//                    TokenResponse(
//                            it.currencySymbol,
//                            it.chain,
//                            it.isToken!!,
//                            it.tokenAddress,
//                            it.tokenName,
//                            it.withdrawAllowed!!,
//                            it.withdrawFee!!,
//                            it.withdrawMin!!,
//                            it.decimal,
//                            it.isActive!!
//                    )
//                }
//    }

//    @PostMapping("/token")
//    suspend fun addCurrencyImplementation(@RequestBody body: TokenRequest): TokenResponse {
//        val ex = OpexError.InvalidRequestBody.exception()
//        with(body) {
//            if (currencySymbol.isNullOrEmpty() || chain.isNullOrEmpty()) throw ex
//            if (isToken && (tokenName.isNullOrEmpty() || tokenAddress.isNullOrEmpty())) throw ex
//            if (withdrawFee < BigDecimal.ZERO || minimumWithdraw < BigDecimal.ZERO || decimal < 0) throw ex
//        }
//
//        return with(service.addToken(body)) {
//            TokenResponse(
//                    currency,
//                    chain,
//                    token,
//                    tokenAddress,
//                    tokenName,
//                    withdrawEnabled,
//                    withdrawFee,
//                    withdrawMin,
//                    decimal,
//                    isActive
//            )
//        }
//    }

//    @PutMapping("/token/{symbol}_{chain}/withdraw")
//    suspend fun changeWithdrawStatus(
//            @PathVariable symbol: String,
//            @PathVariable chain: String,
//            @RequestParam("enabled") status: Boolean
//    ) {
//        service.changeTokenWithdrawStatus(symbol, chain, status)
//    }

}
