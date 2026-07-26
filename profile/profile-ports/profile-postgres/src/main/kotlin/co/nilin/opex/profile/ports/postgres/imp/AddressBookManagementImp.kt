package co.nilin.opex.profile.ports.postgres.imp

import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.core.data.profile.AddressBook
import co.nilin.opex.profile.core.spi.AddressBookPersister
import co.nilin.opex.profile.core.utils.convert
import co.nilin.opex.profile.ports.postgres.dao.AddressBookRepository
import co.nilin.opex.profile.ports.postgres.model.entity.AddressBookModel
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AddressBookManagementImp(
    private val addressBookRepository: AddressBookRepository
) : AddressBookPersister {

    override suspend fun save(addressBook: AddressBook): AddressBook {
        val savedAddressBook =
            (addressBookRepository.save(addressBook.convert(AddressBookModel::class.java)).awaitFirstOrNull()
                ?: throw OpexError.BadRequest.exception("Failed to save address book"))
        return savedAddressBook.convert(AddressBook::class.java)
    }

    override suspend fun findAll(uuid: String): List<AddressBook> {
        return addressBookRepository.findAllByUuid(uuid).collectList().awaitFirstOrElse { emptyList() }
    }

    override suspend fun update(request: AddressBook): AddressBook {
        val addressBook =
            addressBookRepository.findById(request.id!!).awaitFirstOrNull()
                ?: throw OpexError.AddressBookNotFound.exception()
        if (addressBook.uuid == request.uuid) {
            addressBook.apply {
                name = request.name
                address = request.address
                addressType = request.addressType
                updateDate = LocalDateTime.now()
            }
            val savedAddressBook = addressBookRepository.save(addressBook).awaitFirstOrNull()
                ?: throw OpexError.AddressBookNotFound.exception("Failed to update address book")
            return savedAddressBook.convert(AddressBook::class.java)
        } else throw OpexError.Forbidden.exception()
    }

    override suspend fun delete(uuid: String, id: Long) {
        val addressBook =
            addressBookRepository.findById(id).awaitFirstOrNull() ?: throw OpexError.AddressBookNotFound.exception()
        if (addressBook.uuid == uuid)
            addressBookRepository.deleteById(id).awaitFirstOrNull()
        else throw OpexError.Forbidden.exception()
    }

    override suspend fun findSavedAddress(uuid: String, address: String, addressType: String): AddressBook? {
        return addressBookRepository.findByUuidAndAddressAndAddressType(uuid, address, addressType)?.awaitFirstOrNull()
    }
}