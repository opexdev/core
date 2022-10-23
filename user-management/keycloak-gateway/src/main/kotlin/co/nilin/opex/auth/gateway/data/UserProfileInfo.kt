package co.nilin.opex.auth.gateway.data

class UserProfileInfo {

    var firstName: String? = null
    var lastName: String? = null
    var birthday: String? = null
    var idNumber: String? = null
    var mobile: String? = null
    var postalCode: String? = null
    var residence: String? = null
    var nationality: String? = null
    var address: String? = null

    constructor()

    constructor(
        firstName: String?,
        lastName: String?,
        birthday: String?,
        idNumber: String?,
        phoneNumber: String?,
        postalCode: String?,
        residence: String?,
        nationality: String?,
        address: String?
    ) {
        this.firstName = firstName
        this.lastName = lastName
        this.birthday = birthday
        this.idNumber = idNumber
        this.mobile = phoneNumber
        this.postalCode = postalCode
        this.residence = residence
        this.nationality = nationality
        this.address = address
    }
}