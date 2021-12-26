package co.nilin.opex.referral.ports.postgres.impl

import co.nilin.opex.referral.core.model.CommissionReward
import co.nilin.opex.referral.core.model.PaymentRecord
import co.nilin.opex.referral.core.model.PaymentStatuses
import co.nilin.opex.referral.core.spi.CommissionPaymentHandler
import co.nilin.opex.referral.ports.postgres.repository.PaymentRecordRepository
import kotlinx.coroutines.reactive.awaitSingleOrDefault
import org.springframework.stereotype.Service

@Service
class CommissionPaymentHandlerImpl(
    private val paymentRecordRepository: PaymentRecordRepository
) : CommissionPaymentHandler {
    override suspend fun findCommissionsByStatus(paymentStatus: PaymentStatuses): List<PaymentRecord> {
        return paymentRecordRepository.findByPaymentStatusProjected(paymentStatus).map {
            PaymentRecord(
                CommissionReward(
                    it.referrerUuid,
                    it.referentUuid,
                    it.referralCode,
                    Pair(it.richTradeId, null),
                    it.referentOrderDirection,
                    it.referrerShare,
                    it.referentShare
                ),
                it.paymentStatus
            )
        }.collectList().awaitSingleOrDefault(emptyList())
    }

    override suspend fun updatePaymentStatus(id: Long, value: PaymentStatuses) {
        paymentRecordRepository.updatePaymentStatusById(id, value)
    }
}
