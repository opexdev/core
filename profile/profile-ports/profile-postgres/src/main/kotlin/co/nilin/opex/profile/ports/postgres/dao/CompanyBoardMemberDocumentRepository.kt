package co.nilin.opex.profile.ports.postgres.dao

import co.nilin.opex.profile.ports.postgres.model.entity.CompanyBoardMemberDocumentModel
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface CompanyBoardMemberDocumentRepository : ReactiveCrudRepository<CompanyBoardMemberDocumentModel, Long> {

    fun findByCompanyId(companyId: Long): Flux<CompanyBoardMemberDocumentModel>

    @Modifying
    @Query("delete from company_board_member_document where company_id = :companyId")
    fun deleteByCompanyId(companyId: Long): Mono<Int>
}
