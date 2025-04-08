package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.VoucherData
import co.nilin.opex.wallet.core.model.Voucher
import co.nilin.opex.wallet.core.model.VoucherGroup
import co.nilin.opex.wallet.core.model.VoucherGroupType
import co.nilin.opex.wallet.core.model.VoucherSaleData

interface VoucherManager {

    suspend fun getVoucherDataByPublicCode(code: String): VoucherData
    suspend fun getVoucherByPublicCode(code: String): Voucher
    suspend fun getVoucherByPrivateCode(code: String): Voucher
    suspend fun saveVoucherUsage(voucherId: Long, uuid: String)
    suspend fun getVouchers(type: VoucherGroupType?, limit: Int?, offset: Int?): List<VoucherData>
    suspend fun getVoucherGroup(id: Long): VoucherGroup
    suspend fun isExistVoucherUsage(voucherId: Long): Boolean
    suspend fun getUsageCount(uuid: String, voucherGroupId: Long): Long
    suspend fun getUsageCount(voucherId: Long): Long
    suspend fun updateVoucherGroupRemaining(voucherGroupId: Long, remainingVoucherCount: Int)
    suspend fun isExistVoucherSaleData(voucherId: Long): Boolean
    suspend fun saveVoucherSaleData(voucherSaleData: VoucherSaleData)
}