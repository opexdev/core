package co.nilin.opex.profile.core.spi

import co.nilin.opex.profile.core.data.profile.Company
import co.nilin.opex.profile.core.data.profile.LegalAccountRegistration

interface CompanyPersister {
    suspend fun registerLegalAccount(registration: LegalAccountRegistration, creator: String): Company
    suspend fun findByRepresentativeUuid(uuid: String): Company?
}
