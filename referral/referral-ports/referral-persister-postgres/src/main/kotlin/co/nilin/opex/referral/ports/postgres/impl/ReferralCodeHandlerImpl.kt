package co.nilin.opex.referral.ports.postgres.impl

import co.nilin.opex.referral.core.model.ReferralCode
import co.nilin.opex.referral.core.spi.ReferralCodeHandler
import co.nilin.opex.referral.ports.postgres.dao.Reference
import co.nilin.opex.referral.ports.postgres.repository.ReferenceRepository
import co.nilin.opex.referral.ports.postgres.repository.ReferralCodeRepository
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactive.awaitSingleOrDefault
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger

@Service
class ReferralCodeHandlerImpl(
    private val referralCodeRepository: ReferralCodeRepository,
    private val referenceRepository: ReferenceRepository
) : ReferralCodeHandler {
    override suspend fun generateReferralCode(
        uuid: String,
        referentCommission: BigDecimal
    ): String {
        if (referentCommission < BigDecimal.ZERO || referentCommission > BigDecimal.ONE)
            throw IllegalArgumentException("Commission value must be in range of [0, 1]")
        val lastId = referralCodeRepository.findMaxId().awaitSingleOrDefault(-1) + 1
        val codeInteger = BigInteger.TEN.pow(7).toLong() + lastId
        if (codeInteger >= BigInteger.TEN.pow(8).toLong()) throw Exception("No referral code available")
        val code = codeInteger.toString()
        val referralCode = co.nilin.opex.referral.ports.postgres.dao.ReferralCode(null, uuid, code, referentCommission)
        referralCodeRepository.save(referralCode).awaitSingleOrNull()
        return code
    }

    override suspend fun findAll(): List<ReferralCode> {
        return referralCodeRepository.findAll().map { ReferralCode(it.uuid, it.code, it.referentCommission) }
            .collectList().awaitSingle()
    }

    override suspend fun findByReferentUuid(uuid: String): ReferralCode? {
        val referral = referenceRepository.findByReferentUuid(uuid).awaitSingleOrNull() ?: return null
        return referralCodeRepository.findById(referral.referralCodeId)
            .map { ReferralCode(it.uuid, it.code, it.referentCommission) }
            .awaitSingleOrNull()
    }

    override suspend fun findByReferrerUuid(uuid: String): List<ReferralCode> {
        return referralCodeRepository.findByUuid(uuid).map { ReferralCode(it.uuid, it.code, it.referentCommission) }
            .collectList()
            .awaitSingleOrNull()
    }

    override suspend fun findByCode(code: String): ReferralCode? {
        return referralCodeRepository.findByCode(code)
            .map { ReferralCode(it.uuid, it.code, it.referentCommission) }
            .awaitSingle()
    }

    override suspend fun assign(code: String, referentUuid: String) {
        val referralCode = referralCodeRepository.findByCode(code).awaitSingleOrNull()
            ?: throw IllegalArgumentException("Referral code doesn't exist")
        if (referentUuid == referralCode.uuid) throw IllegalArgumentException("Can't assign referral code to referrer")
        val referents = referenceRepository.findByReferrerUuid(referentUuid)
        val isChild = referents.any { it.referentUuid == referralCode.uuid }.awaitSingle()
        if (isChild) throw Exception("Referrer can't be child of referent")
        val reference = Reference(null, referentUuid, referralCode.id!!)
        referenceRepository.save(reference).awaitSingleOrNull()
    }

    override suspend fun updateCommissions(
        code: String,
        referentCommission: BigDecimal
    ) {
        if (referentCommission < BigDecimal.ZERO || referentCommission > BigDecimal.ONE)
            throw IllegalArgumentException("Commission value must be in range of [0, 1]")
        referralCodeRepository.updateByCode(code, referentCommission).awaitSingleOrNull()
    }

    override suspend fun deleteByCode(code: String) {
        referralCodeRepository.deleteByCode(code).awaitSingleOrNull()
    }

    override suspend fun deleteByReferrerUuid(uuid: String) {
        referralCodeRepository.deleteByUuid(uuid).awaitSingleOrNull()
    }
}
