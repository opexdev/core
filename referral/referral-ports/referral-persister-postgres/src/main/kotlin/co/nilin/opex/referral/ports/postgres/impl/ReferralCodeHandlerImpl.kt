package co.nilin.opex.referral.ports.postgres.impl

import co.nilin.opex.referral.core.model.Referral
import co.nilin.opex.referral.core.spi.ReferralCodeHandler
import co.nilin.opex.referral.ports.postgres.dao.Referent
import co.nilin.opex.referral.ports.postgres.dao.ReferralCode
import co.nilin.opex.referral.ports.postgres.repository.ReferralCodeRepository
import co.nilin.opex.referral.ports.postgres.repository.ReferralRepository
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactive.awaitSingleOrDefault
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger

@Service
class ReferralCodeHandlerImpl(
    private val referralCodeRepository: ReferralCodeRepository,
    private val referralRepository: ReferralRepository
) : ReferralCodeHandler {
    override suspend fun findAllReferralCodes(): List<Referral> {
        return referralCodeRepository.findAll().map {
            Referral(it.code, "", it.uuid, it.referrerCommission, it.referentCommission)
        }.collectList().awaitSingle()
    }

    override suspend fun findReferralByUuid(uuid: String): Referral? {
        return referralCodeRepository.findByUuid(uuid)
            .map { Referral(it.code, "", it.uuid, it.referrerCommission, it.referentCommission) }.awaitSingle()
    }

    override suspend fun findReferralCodeByCode(code: String): Referral? {
        return referralCodeRepository.findByCode(code)
            .map { Referral(it.code, "", it.uuid, it.referrerCommission, it.referentCommission) }.awaitSingle()
    }

    override suspend fun generateReferralCode(
        uuid: String,
        referrerCommission: BigDecimal,
        referentCommission: BigDecimal
    ): String {
        if (referrerCommission + referentCommission != BigDecimal.ONE) throw IllegalArgumentException("Sum of commissions must be 1")
        val lastId = referralCodeRepository.findMaxId().awaitSingleOrDefault(0)
        val codeInteger = BigInteger.TEN.pow(7).toLong() + lastId
        if (codeInteger >= BigInteger.TEN.pow(8).toLong()) throw Exception("No referral code available")
        val code = codeInteger.toString()
        val referralCode = ReferralCode(null, code, uuid, referrerCommission, referentCommission)
        referralCodeRepository.save(referralCode)
        return code
    }

    override suspend fun assign(code: String, referentUuid: String): Referral {
        val referralCode = referralCodeRepository.findByCode(code).awaitSingleOrNull()
        if (referralCode != null) {
            val referent = Referent(null, referentUuid, referralCode.id!!)
            referralRepository.save(referent)
            return Referral(
                code,
                referralCode.uuid,
                referentUuid,
                referralCode.referrerCommission,
                referralCode.referentCommission
            )
        } else {
            throw Exception("Referral code doesn't exist")
        }
    }

    override suspend fun updateCommissions(
        code: String,
        referrerCommission: BigDecimal,
        referentCommission: BigDecimal
    ) {
        if (referrerCommission + referentCommission != BigDecimal.ONE) throw IllegalArgumentException("Sum of commissions must be 1")
        referralCodeRepository.updateCommissions(code, referrerCommission, referentCommission)
    }

    override suspend fun deleteReferralCodeByCode(code: String) {
        referralCodeRepository.deleteByCode(code)
    }

    override suspend fun deleteReferralCodeByUuid(uuid: String) {
        referralCodeRepository.deleteByUuid(uuid)
    }
}
