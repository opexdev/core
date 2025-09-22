package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.SubmitVoucherResponse
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.common.security.jwtAuthentication
import co.nilin.opex.common.security.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/opex/v1/voucher")
class VoucherController(private val walletProxy: WalletProxy) {

    @PutMapping("/{code}")
    fun submitVoucher(
        @PathVariable code: String,
        @CurrentSecurityContext securityContext: SecurityContext
    ): SubmitVoucherResponse {
        return walletProxy.submitVoucher(code, securityContext.jwtAuthentication().tokenValue())
    }
}