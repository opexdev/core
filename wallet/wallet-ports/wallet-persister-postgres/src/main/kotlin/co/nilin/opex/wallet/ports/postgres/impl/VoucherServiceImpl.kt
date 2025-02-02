package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.model.Voucher
import co.nilin.opex.wallet.core.model.VoucherStatus
import co.nilin.opex.wallet.core.spi.VoucherManager
import co.nilin.opex.wallet.ports.postgres.dao.VoucherRepository
import co.nilin.opex.wallet.ports.postgres.model.VoucherModel
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class VoucherServiceImpl(
    private val voucherRepository: VoucherRepository
) : VoucherManager {

    private val logger = LoggerFactory.getLogger(VoucherServiceImpl::class.java)

    override suspend fun findByPublicCode(code: String): Voucher? {
        return voucherRepository.findByPublicCode(code).awaitFirstOrNull()?.asVoucher()
            ?: throw OpexError.VoucherNotFound.exception()
    }

    override suspend fun findByPrivateCode(code: String): Voucher? {
        return voucherRepository.findByPrivateCode(code).awaitFirstOrNull()?.asVoucher()
            ?: throw OpexError.VoucherNotFound.exception()
    }

    override suspend fun updateVoucherAsUsed(voucher: Voucher, userId: String): Voucher {
        voucher.apply {
            status = VoucherStatus.USED
            this.userId = userId
            useDate = LocalDateTime.now()
        }
        voucherRepository.save(voucher.asVoucherModel()).awaitFirst()
        return voucher
    }

    private fun VoucherModel.asVoucher(): Voucher {
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
            userId,
            description
        )
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
            userId,
            description
        )
    }
}