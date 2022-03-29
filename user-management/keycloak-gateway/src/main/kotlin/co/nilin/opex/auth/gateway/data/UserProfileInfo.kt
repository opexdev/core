package co.nilin.opex.auth.gateway.data

class UserProfileInfo {

    var firstNameEn: String? = null
    var lastNameEn: String? = null
    var firstNameFa: String? = null
    var lastNameFa: String? = null
    var birthday: String? = null
    var birthdayAlt: String? = null
    var nationalID: String? = null
    var passport: String? = null
    var phoneNumber: String? = null
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
        this.firstNameFa = firstNameFa
        this.lastNameFa = lastNameFa
        this.birthday = birthday
        this.birthdayAlt = birthdayJalali
        this.nationalID = nationalID
        this.passport = passport
        this.phoneNumber = phoneNumber
        this.telephone = homeNumber
        this.postalCode = postalCode
        this.residence = address
        this.nationality = nationality
    }
}