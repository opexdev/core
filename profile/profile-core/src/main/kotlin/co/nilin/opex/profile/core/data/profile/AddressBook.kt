package co.nilin.opex.profile.core.data.profile

import java.time.LocalDateTime

data class AddressBook(
    var id: Long? = null,
    var uuid: String,
    var name: String,
    var address: String,
    var addressType: String,
    var createDate: LocalDateTime? = null,
    var updateDate: LocalDateTime? = null
)