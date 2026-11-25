package co.nilin.opex.profile.core.data.profile

data class CompanySearchFilter(
    val companyName: String? = null,
    val representativeName: String? = null,
    val nationalId: String? = null,
    val registrationNumber: String? = null,
    val createDateFrom: Long? = null,
    val createDateTo: Long? = null,
    val limit: Int = 10,
    val offset: Int = 0
)
