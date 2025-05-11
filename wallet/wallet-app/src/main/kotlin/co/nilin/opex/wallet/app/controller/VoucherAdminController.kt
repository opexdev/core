package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.SellVoucherRequest
import co.nilin.opex.wallet.app.dto.VoucherSaleDataResponse
import co.nilin.opex.wallet.app.dto.VoucherUsageDataResponse
import co.nilin.opex.wallet.app.service.VoucherService
import co.nilin.opex.wallet.core.inout.VoucherData
import co.nilin.opex.wallet.core.model.VoucherGroupType
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin/voucher")
class VoucherAdminController(private val voucherService: VoucherService) {

    @GetMapping("/{code}")
    suspend fun getVoucher(@PathVariable code: String): VoucherData {
        return voucherService.getVoucher(code)
    }

    @GetMapping
    suspend fun getVouchers(
        @RequestParam type: VoucherGroupType?,
        @RequestParam issuer: String?,
        @RequestParam isUsed: Boolean?,
        @RequestParam limit: Int?,
        @RequestParam offset: Int?,
    ): List<VoucherData> {
        return voucherService.getVouchers(type, issuer, isUsed, limit, offset)
    }

    @PostMapping("/sell")
    suspend fun sellVoucher(
        @RequestBody request: SellVoucherRequest,
        @CurrentSecurityContext securityContext: SecurityContext,
    ) {
        return voucherService.sellVoucher(request, securityContext.authentication.name)
    }

    @GetMapping("/sell/{code}")
    suspend fun getVoucherSaleData(
        @PathVariable code: String,
    ): VoucherSaleDataResponse {
        return voucherService.getVoucherSaleData(code)
    }

    @GetMapping("/usage/{code}")
    suspend fun getVoucherUsageData(@PathVariable code: String): List<VoucherUsageDataResponse> {
        return voucherService.getVoucherUsageData(code)
    }
}