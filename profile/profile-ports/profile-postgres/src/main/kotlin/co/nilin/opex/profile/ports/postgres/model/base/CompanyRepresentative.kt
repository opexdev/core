package co.nilin.opex.profile.ports.postgres.model.base

import java.time.LocalDateTime

open class CompanyRepresentative {
    var companyId: Long = 0
    lateinit var uuid: String
    lateinit var createDate: LocalDateTime
    var creator: String? = null
}
