package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.VoucherData
import co.nilin.opex.wallet.core.model.Voucher

interface VoucherManager {

    suspend fun findByPublicCode(code: String): VoucherData?
    suspend fun findByPrivateCode(code: String): Voucher?
    suspend fun updateVoucherAsUsed(voucher: Voucher, uuid: String): Voucher
}