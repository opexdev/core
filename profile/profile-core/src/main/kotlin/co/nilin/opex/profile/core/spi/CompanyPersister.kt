package co.nilin.opex.profile.core.spi

import co.nilin.opex.profile.core.data.profile.Company
import java.util.UUID
import co.nilin.opex.profile.core.data.profile.CompanyUpdate
import co.nilin.opex.profile.core.data.profile.LegalAccountAvailability
import co.nilin.opex.profile.core.data.profile.LegalAccountRegistration

interface CompanyPersister {

    suspend fun ensureAvailable(availability: LegalAccountAvailability)
    suspend fun registerLegalAccount(registration: LegalAccountRegistration, creator: String): Company
    suspend fun findByRepresentativeUuid(uuid: String): Company?
    suspend fun findByCompanyNationalId(nationalId: String): Company?
    suspend fun findByUuid(companyUuid: UUID): Company?
    suspend fun updateByUuid(companyUuid: UUID, update: CompanyUpdate): Company
}
