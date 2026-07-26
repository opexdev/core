package co.nilin.opex.profile.ports.postgres.model.entity

import co.nilin.opex.profile.ports.postgres.model.base.AddressBook
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("address_book")
data class AddressBookModel(
    @Id var id: Long
) : AddressBook()
