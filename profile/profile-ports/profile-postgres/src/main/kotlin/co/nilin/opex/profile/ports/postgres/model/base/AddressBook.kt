package co.nilin.opex.profile.ports.postgres.model.base

import java.time.LocalDateTime

open class AddressBook {
    lateinit var uuid: String
    lateinit var name: String
    lateinit var address: String
    lateinit var addressType: String
    lateinit var createDate: LocalDateTime
    var updateDate: LocalDateTime? = null
}