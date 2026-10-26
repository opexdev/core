package co.nilin.opex.profile.ports.postgres.dao

import co.nilin.opex.profile.ports.postgres.model.entity.CompanyBoardMemberDocumentModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface CompanyBoardMemberDocumentRepository : ReactiveCrudRepository<CompanyBoardMemberDocumentModel, Long> {

    fun findByCompanyId(companyId: Long): Flux<CompanyBoardMemberDocumentModel>
}
