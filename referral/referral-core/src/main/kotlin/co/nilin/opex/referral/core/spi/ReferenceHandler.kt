package co.nilin.opex.referral.core.spi

import co.nilin.opex.referral.core.model.Reference

interface ReferenceHandler {
    suspend fun findAll(): List<Reference>
    suspend fun findByReferentUuid(uuid: String): Reference?
    suspend fun findByReferrerUuid(uuid: String): List<Reference>
    suspend fun findByCode(code: String): List<Reference>
}
