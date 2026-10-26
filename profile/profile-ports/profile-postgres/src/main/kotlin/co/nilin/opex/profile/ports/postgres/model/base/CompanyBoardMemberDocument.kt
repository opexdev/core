package co.nilin.opex.profile.ports.postgres.model.base

import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime

open class CompanyBoardMemberDocument {
    var companyId: Long = 0

    @Column("s3_key")
    lateinit var s3Key: String
    lateinit var createDate: LocalDateTime
}
