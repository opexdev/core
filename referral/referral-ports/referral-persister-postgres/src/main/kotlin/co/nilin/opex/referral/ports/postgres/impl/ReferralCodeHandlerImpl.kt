package co.nilin.opex.referral.ports.postgres.impl

import co.nilin.opex.referral.core.model.Referral
import co.nilin.opex.referral.core.spi.ReferralCodeHandler
import co.nilin.opex.referral.ports.postgres.dao.ReferralCode
import co.nilin.opex.referral.ports.postgres.repository.ReferralCodeRepository
import co.nilin.opex.referral.ports.postgres.repository.ReferralRepository
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class ReferralCodeHandlerImpl(
    private val referralCodeRepository: ReferralCodeRepository,
    private val referralRepository: ReferralRepository
) : ReferralCodeHandler {
    override suspend fun findAllReferrals(): List<Referral> {
        return referralCodeRepository.findAll().map {
            Referral(it.code, "", it.uuid, it.referrerCommission, it.referentCommission)
        }.collectList().awaitSingle()
    }

    override suspend fun findReferralByUuid(uuid: String): Referral? {
        return referralCodeRepository.findByUuid(uuid)
            .map { Referral(it.code, "", it.uuid, it.referrerCommission, it.referentCommission) }.awaitSingle()
    }

    override suspend fun findReferralByCode(code: String): Referral? {
        return referralCodeRepository.findByCode(code)
            .map { Referral(it.code, "", it.uuid, it.referrerCommission, it.referentCommission) }.awaitSingle()
    }

    override suspend fun generateReferralCode(
        uuid: String,
        referrerCommission: BigDecimal,
        referentCommission: BigDecimal
    ) {
        val code = ""
        val referralCode = ReferralCode(null, code, uuid, referrerCommission, referentCommission)
        referralCodeRepository.save(referralCode)
    }

    override suspend fun assign(code: String, referentUuid: String): Referral {
        val referralCode = referralCodeRepository.findByCode(code).awaitSingleOrNull()
        if (referralCode != null) {
            val referral = co.nilin.opex.referral.ports.postgres.dao.Referral(null, code, referentUuid)
            referralRepository.save(referral)
            return Referral(
                code,
                referralCode.uuid,
                referentUuid,
                referralCode.referrerCommission,
                referralCode.referentCommission
            )
        }
        throw Exception("Referral code does exist")
    }

    override suspend fun updateCommissions(
        code: String,
        referrerCommission: BigDecimal?,
        referentCommission: BigDecimal?
    ) {
        referralCodeRepository.updateCommissions(code, referrerCommission, referentCommission)
    }

    override suspend fun deleteReferralCode(code: String) {
        referralCodeRepository.deleteByCode(code)
    }

    override suspend fun deleteReferralCodeByUuid(uuid: String) {
        referralCodeRepository.deleteByUuid(uuid)
    }
}
