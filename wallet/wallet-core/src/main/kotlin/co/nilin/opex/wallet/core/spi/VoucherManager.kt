package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.VoucherData
import co.nilin.opex.wallet.core.model.Voucher
import co.nilin.opex.wallet.core.model.VoucherStatus

interface VoucherManager {

    suspend fun findByPublicCode(code: String): VoucherData?
    suspend fun findByPrivateCode(code: String): Voucher?
    suspend fun updateVoucherAsUsed(voucher: Voucher, uuid: String): Voucher
    suspend fun findAll(status: VoucherStatus?, limit: Int?, offset: Int?): List<VoucherData>
}