package co.nilin.opex.referral.ports.postgres.impl

import co.nilin.opex.referral.core.model.CommissionReward
import co.nilin.opex.referral.core.model.PaymentRecord
import co.nilin.opex.referral.core.model.PaymentStatuses
import co.nilin.opex.referral.core.spi.CommissionPaymentHandler
import co.nilin.opex.referral.ports.postgres.repository.PaymentRecordRepository
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactive.awaitSingleOrDefault
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class CommissionPaymentHandlerImpl(
    private val paymentRecordRepository: PaymentRecordRepository
) : CommissionPaymentHandler {
    override suspend fun findCommissionsByStatus(paymentStatus: PaymentStatuses): List<PaymentRecord> {
        return paymentRecordRepository.findByPaymentStatusProjected(paymentStatus).map {
            PaymentRecord(
                CommissionReward(
                    it.rewardedUuid,
                    it.referentUuid,
                    it.referralCode,
                    Pair(it.richTradeId, null),
                    it.referentOrderDirection,
                    it.share,
                    it.paymentAssetSymbol,
                    it.createDate
                ),
                it.paymentStatus,
                it.updateDate
            )
        }.collectList().awaitSingleOrDefault(emptyList())
    }

    override suspend fun findUserCommissionsWhereTotalGreaterAndEqualTo(
        uuid: String,
        value: BigDecimal
    ): List<PaymentRecord> {
        return paymentRecordRepository.findWhereTotalReferrerShareMoreThanProjected(uuid, value)
            .collectList().awaitSingle().map {
                PaymentRecord(
                    CommissionReward(
                        it.rewardedUuid,
                        it.referentUuid,
                        it.referralCode,
                        Pair(it.richTradeId, null),
                        it.referentOrderDirection,
                        it.share,
                        it.paymentAssetSymbol,
                        it.createDate
                    ),
                    it.paymentStatus,
                    it.updateDate
                )
            }
    }

    override suspend fun findCommissionsWherePendingDateLessOrEqualThan(date: Long): List<PaymentRecord> {
        return paymentRecordRepository.findByPaymentStatusWhereCreateDateLessThanProjected(
            PaymentStatuses.PENDING,
            date
        ).collectList().awaitSingle().map {
            PaymentRecord(
                CommissionReward(
                    it.rewardedUuid,
                    it.referentUuid,
                    it.referralCode,
                    Pair(it.richTradeId, null),
                    it.referentOrderDirection,
                    it.share,
                    it.paymentAssetSymbol,
                    it.createDate
                ),
                it.paymentStatus,
                it.updateDate
            )
        }
    }

    override suspend fun updatePaymentStatus(id: Long, value: PaymentStatuses) {
        paymentRecordRepository.updatePaymentStatusById(id, value)
    }
}
