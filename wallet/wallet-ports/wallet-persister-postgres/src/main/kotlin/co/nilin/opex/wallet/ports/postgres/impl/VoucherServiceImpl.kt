package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.inout.VoucherData
import co.nilin.opex.wallet.core.inout.VoucherGroupData
import co.nilin.opex.wallet.core.model.Voucher
import co.nilin.opex.wallet.core.model.VoucherGroup
import co.nilin.opex.wallet.core.model.VoucherGroupStatus
import co.nilin.opex.wallet.core.spi.VoucherManager
import co.nilin.opex.wallet.ports.postgres.dao.VoucherGroupRepository
import co.nilin.opex.wallet.ports.postgres.dao.VoucherRepository
import co.nilin.opex.wallet.ports.postgres.dao.VoucherUsageRepository
import co.nilin.opex.wallet.ports.postgres.model.VoucherGroupModel
import co.nilin.opex.wallet.ports.postgres.model.VoucherModel
import co.nilin.opex.wallet.ports.postgres.model.VoucherUsageModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class VoucherServiceImpl(
    private val voucherRepository: VoucherRepository,
    private val voucherGroupRepository: VoucherGroupRepository,
    private val voucherUsageRepository: VoucherUsageRepository,
) : VoucherManager {

    private val logger = LoggerFactory.getLogger(VoucherServiceImpl::class.java)

    override suspend fun findByPublicCode(code: String): VoucherData? {
        return voucherRepository.findByPublicCode(code).awaitFirstOrNull()?.asVoucherData()
            ?: throw OpexError.VoucherNotFound.exception()
    }

    override suspend fun findByPrivateCode(code: String): Voucher? {
        return voucherRepository.findByPrivateCode(code).awaitFirstOrNull()?.asVoucher()
            ?: throw OpexError.VoucherNotFound.exception()
    }

    override suspend fun saveVoucherUsage(voucherId: Long, uuid: String) {
        val voucherUsageModel = VoucherUsageModel(null, voucherId, LocalDateTime.now(), uuid)
        voucherUsageRepository.save(voucherUsageModel).awaitFirst()
    }

    override suspend fun findAll(
        status: VoucherGroupStatus?,
        limit: Int?,
        offset: Int?,
    ): List<VoucherData> {
        return voucherRepository.findAll(status, limit, offset).map { it.asVoucherData() }.toList()
    }

    override suspend fun findVoucherGroup(id: Long): VoucherGroup? {
        return voucherGroupRepository.findById(id).awaitFirstOrNull()?.asVoucherGroup()
            ?: throw OpexError.VoucherGroupNotFound.exception()
    }

    override suspend fun isExistVoucherUsage(voucherId: Long): Boolean {
        return voucherUsageRepository.existsVoucherUsage(voucherId).awaitFirst()
    }

    override suspend fun findUsageCount(uuid: String, voucherGroupId: Long): Long {
        return voucherUsageRepository.count(uuid, voucherGroupId).awaitFirst()
    }

    override suspend fun findUsageCount(voucherId: Long): Long {
        return voucherUsageRepository.count(voucherId).awaitFirst()
    }

    override suspend fun updateVoucherGroupRemaining(voucherGroupId: Long, remainingUsage: Int) {
        voucherGroupRepository.updateRemaining(voucherGroupId,remainingUsage).awaitFirstOrNull()
    }

    private suspend fun VoucherModel.asVoucher(): Voucher {
        return Voucher(
            id,
            privateCode,
            publicCode,
            amount,
            currency,
            expireDate,
            createDate,
            voucherGroup =
                voucherGroupRepository.findById(voucherGroup).awaitFirstOrNull()?.asVoucherGroup()
                    ?: throw OpexError.VoucherGroupNotFound.exception()

        )
    }

    private suspend fun VoucherModel.asVoucherData(): VoucherData {
        return VoucherData(
            publicCode,
            amount,
            currency,
            expireDate,
            createDate,
            voucherGroup =
                voucherGroupRepository.findById(voucherGroup).awaitFirstOrNull()?.asVoucherGroupData(),
            usageCount = id?.let { voucherUsageRepository.count(it).awaitFirst() } ?: throw OpexError.VoucherNotFound.exception()
        )
    }

    private fun VoucherGroupModel.asVoucherGroup(): VoucherGroup {
        return VoucherGroup(
            id,
            issuer,
            description,
            status,
            type,
            remainingUsage,
            userLimit,
            version
        )
    }

    private fun VoucherGroupModel.asVoucherGroupData(): VoucherGroupData {
        return VoucherGroupData(
            issuer,
            description,
            status,
            type,
            remainingUsage,
            userLimit,
        )
    }
}