package co.nilin.opex.profile.ports.postgres.model.entity

import co.nilin.opex.profile.ports.postgres.model.base.CompanyBoardMemberDocument
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("company_board_member_document")
data class CompanyBoardMemberDocumentModel(
    @Id var id: Long? = null
) : CompanyBoardMemberDocument()
