package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.model.Voucher

interface VoucherManager {

    suspend fun findByPublicCode(code :String) : Voucher?
    suspend fun findByPrivateCode(code :String) : Voucher?
    suspend fun updateVoucherAsUsed(voucher: Voucher , userId : String) : Voucher
}