package co.nilin.opex.referral.ports.postgres.impl

import co.nilin.opex.referral.core.model.CheckoutRecord
import co.nilin.opex.referral.core.model.CheckoutState
import co.nilin.opex.referral.core.model.CommissionReward
import co.nilin.opex.referral.core.spi.CommissionPaymentHandler
import co.nilin.opex.referral.ports.postgres.repository.CheckoutRecordRepository
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactive.awaitSingleOrDefault
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
class CommissionPaymentHandlerImpl(private val checkoutRecordRepository: CheckoutRecordRepository) :
    CommissionPaymentHandler {
    override suspend fun findCommissionsByCheckoutState(checkoutState: CheckoutState): List<CheckoutRecord> {
        return checkoutRecordRepository.findByCheckoutStateProjected(checkoutState).map {
            CheckoutRecord(
                CommissionReward(
                    it.commissionRewardsId,
                    it.rewardedUuid,
                    it.referentUuid,
                    it.referralCode,
                    Pair(it.richTradeId, null),
                    it.referentOrderDirection,
                    it.share,
                    it.createDate
                ),
                it.checkoutState,
                it.transferRef,
                it.updateDate
            )
        }.collectList().awaitSingleOrDefault(emptyList())
    }

    override suspend fun findUserCommissionsWhereTotalGreaterAndEqualTo(
        uuid: String,
        value: BigDecimal
    ): List<CheckoutRecord> {
        return checkoutRecordRepository.findByUuidWhereTotalShareMoreThanProjected(uuid, value)
            .collectList().awaitSingle().map {
                CheckoutRecord(
                    CommissionReward(
                        it.commissionRewardsId,
                        it.rewardedUuid,
                        it.referentUuid,
                        it.referralCode,
                        Pair(it.richTradeId, null),
                        it.referentOrderDirection,
                        it.share,
                        it.createDate
                    ),
                    it.checkoutState,
                    it.transferRef,
                    it.updateDate
                )
            }
    }

    override suspend fun findAllCommissionsWhereTotalGreaterAndEqualTo(value: BigDecimal): List<CheckoutRecord> {
        return checkoutRecordRepository.findAllWhereTotalShareMoreThanProjected(value)
            .collectList().awaitSingle().map {
                CheckoutRecord(
                    CommissionReward(
                        it.commissionRewardsId,
                        it.rewardedUuid,
                        it.referentUuid,
                        it.referralCode,
                        Pair(it.richTradeId, null),
                        it.referentOrderDirection,
                        it.share,
                        it.createDate
                    ),
                    it.checkoutState,
                    it.transferRef,
                    it.updateDate
                )
            }
    }

    override suspend fun findCommissionsWherePendingDateLessOrEqualThan(date: Date): List<CheckoutRecord> {
        return checkoutRecordRepository.findByCheckoutStateWhereCreateDateLessThanProjected(
            CheckoutState.PENDING,
            date
        ).collectList().awaitSingle().map {
            CheckoutRecord(
                CommissionReward(
                    it.commissionRewardsId,
                    it.rewardedUuid,
                    it.referentUuid,
                    it.referralCode,
                    Pair(it.richTradeId, null),
                    it.referentOrderDirection,
                    it.share,
                    it.createDate
                ),
                it.checkoutState,
                it.transferRef,
                it.updateDate
            )
        }
    }

    override suspend fun updateCheckoutState(id: Long, value: CheckoutState) {
        checkoutRecordRepository.updateCheckoutStateById(id, value)
    }

    override suspend fun checkout(id: Long, transferRef: String) {
        checkoutRecordRepository.checkout(id, transferRef)
    }
}
