package co.nilin.opex.referral.ports.postgres.impl

import co.nilin.opex.referral.core.model.Reference
import co.nilin.opex.referral.core.model.ReferralCode
import co.nilin.opex.referral.core.spi.ReferenceHandler
import co.nilin.opex.referral.ports.postgres.repository.ReferenceRepository
import co.nilin.opex.referral.ports.postgres.repository.ReferralCodeRepository
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.stereotype.Service

@Service
class ReferenceHandlerImpl(
    private val referralCodeRepository: ReferralCodeRepository,
    private val referenceRepository: ReferenceRepository
) : ReferenceHandler {
    override suspend fun findAll(): List<Reference> {
        val refs = referenceRepository.findAll().collectList().awaitSingle()
        return refs.map { ref ->
            val referralCode = referralCodeRepository.findById(ref.referralCodeId).map {
                ReferralCode(it.uuid, it.code, it.referentCommission)
            }.awaitSingle()
            Reference(referralCode, ref.referentUuid)
        }
    }

    override suspend fun findByReferentUuid(uuid: String): Reference? {
        val ref = referenceRepository.findByUuid(uuid).awaitSingleOrNull() ?: return null
        val referralCode = referralCodeRepository.findById(ref.referralCodeId).map {
            ReferralCode(it.uuid, it.code, it.referentCommission)
        }.awaitSingle()
        return Reference(referralCode, ref.referentUuid)
    }

    override suspend fun findByReferrerUuid(uuid: String): List<Reference> {
        val ref = referenceRepository.findByReferrerUuid(uuid).collectList().awaitSingle()
        val referralCode = referralCodeRepository.findByUuid(uuid).map {
            ReferralCode(it.uuid, it.code, it.referentCommission)
        }.awaitSingle()
        return ref.map { Reference(referralCode, it.referentUuid) }
    }


    override suspend fun findByCode(code: String): List<Reference> {
        val ref = referenceRepository.findByCode(code).collectList().awaitSingle()
        val referralCode = referralCodeRepository.findByCode(code).map {
            ReferralCode(it.uuid, it.code, it.referentCommission)
        }.awaitSingle()
        return ref.map { Reference(referralCode, it.referentUuid) }
    }
}
