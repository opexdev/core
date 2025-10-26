package co.nilin.opex.profile.app.service

import co.nilin.opex.profile.app.dto.AddAddressBookItemRequest
import co.nilin.opex.profile.app.dto.AddressBookResponse
import co.nilin.opex.profile.app.utils.toAddressBookResponse
import co.nilin.opex.profile.core.data.profile.AddressBook
import co.nilin.opex.profile.core.spi.AddressBookPersister
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class AddressBookManagement(
    private val addressBookPersister: AddressBookPersister,
) {
    suspend fun addAddressBook(uuid: String, request: AddAddressBookItemRequest) {
        addressBookPersister.save(
            AddressBook(
                uuid = uuid,
                name = request.name,
                address = request.address,
                addressType = request.addressType,
                createDate = LocalDateTime.now(),
            )
        )
    }

    suspend fun getAllAddressBooks(uuid: String): List<AddressBookResponse> {
        return addressBookPersister.findAll(uuid).map { it.toAddressBookResponse() }
    }

    suspend fun updateAddressBook(uuid: String, id: Long, request: AddAddressBookItemRequest) {
        addressBookPersister.update(
            AddressBook(
                id = id,
                uuid = uuid,
                name = request.name,
                address = request.address,
                addressType = request.addressType,
            )
        )
    }

    suspend fun deleteAddressBook(uuid: String, id: Long) {
        addressBookPersister.delete(uuid, id)
    }
}
