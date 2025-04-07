package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.VoucherData
import co.nilin.opex.wallet.core.model.Voucher
import co.nilin.opex.wallet.core.model.VoucherGroup
import co.nilin.opex.wallet.core.model.VoucherGroupStatus
import co.nilin.opex.wallet.core.model.VoucherUsage

interface VoucherManager {

    suspend fun findByPublicCode(code: String): VoucherData?
    suspend fun findByPrivateCode(code: String): Voucher?
    suspend fun saveVoucherUsage(voucherId: Long, uuid: String)
    suspend fun findAll(status: VoucherGroupStatus?, limit: Int?, offset: Int?): List<VoucherData>
    suspend fun findVoucherGroup(id: Long): VoucherGroup?
    suspend fun isExistVoucherUsage(voucherId : Long): Boolean
    suspend fun findUsageCount(uuid: String , voucherGroupId : Long): Long
    suspend fun findUsageCount(voucherId: Long): Long
    suspend fun updateVoucherGroupRemaining(voucherGroupId: Long, remainingVoucherCount: Int)
}