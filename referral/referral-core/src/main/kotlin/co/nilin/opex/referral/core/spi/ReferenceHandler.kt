package co.nilin.opex.referral.core.spi

import co.nilin.opex.referral.core.model.Reference

interface ReferenceHandler {
    suspend fun findAll(): List<Reference>
    suspend fun findByUuid(uuid: String): Reference?
    suspend fun findByCode(code: String): Reference?
}
