package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.inout.VoucherData
import co.nilin.opex.wallet.core.model.Voucher
import co.nilin.opex.wallet.core.model.VoucherGroup
import co.nilin.opex.wallet.core.model.VoucherStatus
import co.nilin.opex.wallet.core.spi.VoucherManager
import co.nilin.opex.wallet.ports.postgres.dao.VoucherGroupRepository
import co.nilin.opex.wallet.ports.postgres.dao.VoucherRepository
import co.nilin.opex.wallet.ports.postgres.model.VoucherGroupModel
import co.nilin.opex.wallet.ports.postgres.model.VoucherModel
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class VoucherServiceImpl(
    private val voucherRepository: VoucherRepository,
    private val voucherGroupRepository: VoucherGroupRepository
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

    override suspend fun updateVoucherAsUsed(voucher: Voucher, uuid: String): Voucher {
        voucher.apply {
            status = VoucherStatus.USED
            this.uuid = uuid
            useDate = LocalDateTime.now()
        }
        voucherRepository.save(voucher.asVoucherModel()).awaitFirst()
        return voucher
    }

    private suspend fun VoucherModel.asVoucher(): Voucher {
        return Voucher(
            id,
            privateCode,
            publicCode,
            amount,
            currency,
            status,
            expireDate,
            createDate,
            useDate,
            uuid,
            voucherGroup = voucherGroup?.let {
                voucherGroupRepository.findById(it).awaitFirstOrNull()?.asVoucherGroup()
            }
        )
    }

    private suspend fun VoucherModel.asVoucherData(): VoucherData {
        return VoucherData(
            publicCode,
            amount,
            currency,
            status,
            expireDate,
            createDate,
            useDate,
            uuid,
            voucherGroup = voucherGroup?.let {
                voucherGroupRepository.findById(it).awaitFirstOrNull()?.asVoucherGroup()
            })
    }

    private fun Voucher.asVoucherModel(): VoucherModel {
        return VoucherModel(
            id,
            privateCode,
            publicCode,
            amount,
            currency,
            status,
            expireDate,
            createDate,
            useDate,
            uuid,
            voucherGroup = voucherGroup?.id
        )
    }

    private fun VoucherGroupModel.asVoucherGroup(): VoucherGroup {
        return VoucherGroup(
            id,
            issuer,
            description
        )
    }
}