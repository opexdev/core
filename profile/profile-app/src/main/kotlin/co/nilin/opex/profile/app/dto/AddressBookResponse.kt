package co.nilin.opex.profile.app.dto

data class AddressBookResponse(
    var id: Long? = null,
    var name: String,
    var address: String,
    var addressType: String,
)
