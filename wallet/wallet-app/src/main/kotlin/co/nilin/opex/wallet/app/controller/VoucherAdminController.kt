package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.service.VoucherService
import co.nilin.opex.wallet.core.model.Voucher
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin/voucher")
class VoucherAdminController(private val voucherService: VoucherService) {

    @GetMapping("/{code}")
    suspend fun getVoucher(@PathVariable code: String): Voucher {
        return voucherService.getVoucher(code)
    }
}