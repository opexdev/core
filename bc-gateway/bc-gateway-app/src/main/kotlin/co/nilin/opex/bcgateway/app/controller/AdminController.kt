package co.nilin.opex.bcgateway.app.controller

import co.nilin.opex.bcgateway.app.dto.AddChainRequest
import co.nilin.opex.bcgateway.app.dto.AddressTypeRequest
import co.nilin.opex.bcgateway.app.dto.AssetRequest
import co.nilin.opex.bcgateway.app.service.AdminService
import co.nilin.opex.bcgateway.core.model.CurrencyImplementation
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin")
class AdminController(private val service: AdminService) {

    @PostMapping("/chain")
    suspend fun addChain(@RequestBody body: AddChainRequest) {
        if (!body.isValid())
            throw OpexException(OpexError.InvalidRequestBody)
        service.addChain(body)
    }

    @PostMapping("/address/type")
    suspend fun addAddressType(@RequestBody body: AddressTypeRequest) {
        if (body.name.isNullOrEmpty() || body.addressRegex.isNullOrEmpty())
            throw OpexException(OpexError.InvalidRequestBody)
        service.addAddressType(body.name, body.addressRegex, body.memoRegex)
    }

    @PostMapping("/asset")
    suspend fun addCurrencyImplementation(@RequestBody body: AssetRequest): CurrencyImplementation {
        val ex = OpexException(OpexError.InvalidRequestBody)
        with(body) {
            if (symbol.isNullOrEmpty() || chain.isNullOrEmpty()) throw ex
            if (isToken && (tokenName.isNullOrEmpty() || tokenAddress.isNullOrEmpty())) throw ex
            if (withdrawFee < 0 || minimumWithdraw < 0 || decimal < 1) throw ex
        }

        return service.addAsset(body)
    }

}