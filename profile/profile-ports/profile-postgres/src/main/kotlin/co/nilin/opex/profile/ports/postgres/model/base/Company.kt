package co.nilin.opex.profile.ports.postgres.model.base

import java.time.LocalDateTime

open class Company {
    lateinit var registrationNumber: String
    lateinit var nationalId: String
    var representativeNationalCardKey: String? = null
    var introductionLetterKey: String? = null
    var articlesOfAssociationKey: String? = null
    var officialNewspaperKey: String? = null
    lateinit var createDate: LocalDateTime
    var updateDate: LocalDateTime? = null
    var creator: String? = null
}
