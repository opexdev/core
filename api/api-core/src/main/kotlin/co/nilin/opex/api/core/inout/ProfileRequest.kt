package co.nilin.opex.api.core.inout

data class ProfileRequest(
    var userId: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var mobile: String? = null,
    var email: String? = null,
    var identifier: String? = null,
    var nationality: NationalityType? = null,
    var gender: Gender? = null,
    var status: ProfileStatus? = null,
    var kycLevel: KycLevel? = null,
    var createDateFrom: Long? = null,
    var createDateTo: Long? = null,
    var limit : Int = 10,
    var offset: Int = 0,
    var ascendingByTime: Boolean = false,)
