package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.service.VoucherService
import co.nilin.opex.wallet.core.inout.SubmitVoucherResponse
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/voucher")
class VoucherController(private val voucherService: VoucherService) {

    @PutMapping("/{code}")
    suspend fun submitVoucher(
        @PathVariable code: String,
        @CurrentSecurityContext securityContext: SecurityContext
    ): SubmitVoucherResponse {
        return voucherService.submitVoucher(securityContext.authentication.name, code)
    }
}