package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.service.VoucherService
import co.nilin.opex.wallet.core.inout.VoucherData
import co.nilin.opex.wallet.core.model.VoucherStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin/voucher")
class VoucherAdminController(private val voucherService: VoucherService) {

    @GetMapping("/{code}")
    suspend fun getVoucher(@PathVariable code: String): VoucherData {
        return voucherService.getVoucher(code)
    }

    @GetMapping
    suspend fun getVoucher(
        @RequestParam status: VoucherStatus?,
        @RequestParam limit: Int?,
        @RequestParam offset: Int?
    ): List<VoucherData> {
        return voucherService.getVouchers(status, limit, offset)
    }
}