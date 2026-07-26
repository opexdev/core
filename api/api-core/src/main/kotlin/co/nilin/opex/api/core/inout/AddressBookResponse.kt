package co.nilin.opex.api.core.inout

data class AddressBookResponse(
    var id: Long? = null,
    var name: String,
    var address: String,
    var addressType: String,
)
