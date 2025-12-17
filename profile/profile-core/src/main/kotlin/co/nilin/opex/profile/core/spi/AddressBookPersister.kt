package co.nilin.opex.profile.core.spi

import co.nilin.opex.profile.core.data.profile.AddressBook

interface AddressBookPersister {

    suspend fun save(addressBook: AddressBook): AddressBook
    suspend fun findAll(uuid: String): List<AddressBook>
    suspend fun update(addressBook: AddressBook): AddressBook
    suspend fun delete(uuid: String, id: Long)
    suspend fun findSavedAddress(uuid: String, address: String, adressType: String): AddressBook?

}