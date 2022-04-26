package co.nilin.opex.auth.gateway.data

class UserProfileInfo {

    var firstNameEn: String? = null
    var lastNameEn: String? = null
    var firstName: String? = null
    var lastName: String? = null
    var birthdayJ: String? = null
    var birthdayG: String? = null
    var nationalId: String? = null
    var passportNumber: String? = null
    var mobile: String? = null
    var telephone: String? = null
    var postalCode: String? = null
    var residence: String? = null
    var nationality: String? = null

    constructor()

    constructor(
        firstNameEn: String?,
        lastNameEn: String?,
        firstNameFa: String?,
        lastNameFa: String?,
        birthday: String?,
        birthdayJalali: String?,
        nationalID: String?,
        passport: String?,
        phoneNumber: String?,
        homeNumber: String?,
        postalCode: String?,
        address: String?,
        nationality: String?
    ) {
        this.firstNameEn = firstNameEn
        this.lastNameEn = lastNameEn
        this.firstName = firstNameFa
        this.lastName = lastNameFa
        this.birthdayJ = birthday
        this.birthdayG = birthdayJalali
        this.nationalId = nationalID
        this.passportNumber = passport
        this.mobile = phoneNumber
        this.telephone = homeNumber
        this.postalCode = postalCode
        this.residence = address
        this.nationality = nationality
    }
}