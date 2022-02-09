package co.nilin.opex.auth.gateway.data

class UserProfileInfo {

    var firstNameEn: String? = null
    var lastNameEn: String? = null
    var firstNameFa: String? = null
    var lastNameFa: String? = null
    var birthday: String? = null
    var birthdayJalali: String? = null
    var nationalID: String? = null
    var passport: String? = null
    var phoneNumber: String? = null
    var homeNumber: String? = null
    var email: String? = null
    var postalCode: String? = null
    var address: String? = null

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
        email: String?,
        postalCode: String?,
        address: String?
    ) {
        this.firstNameEn = firstNameEn
        this.lastNameEn = lastNameEn
        this.firstNameFa = firstNameFa
        this.lastNameFa = lastNameFa
        this.birthday = birthday
        this.birthdayJalali = birthdayJalali
        this.nationalID = nationalID
        this.passport = passport
        this.phoneNumber = phoneNumber
        this.homeNumber = homeNumber
        this.email = email
        this.postalCode = postalCode
        this.address = address
    }
}