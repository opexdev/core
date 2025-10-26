package co.nilin.opex.profile.ports.postgres.imp

import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.core.data.profile.AddressBook
import co.nilin.opex.profile.core.spi.AddressBookPersister
import co.nilin.opex.profile.core.utils.convert
import co.nilin.opex.profile.ports.postgres.dao.AddressBookRepository
import co.nilin.opex.profile.ports.postgres.model.entity.AddressBookModel
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AddressBookManagementImp(
    private val addressBookRepository: AddressBookRepository
) : AddressBookPersister {
    private val logger = LoggerFactory.getLogger(AddressBookManagementImp::class.java)

    override suspend fun save(addressBook: AddressBook) {
        addressBookRepository.save(addressBook.convert(AddressBookModel::class.java)).awaitFirst()
    }

    override suspend fun findAll(uuid: String): List<AddressBook> {
        return addressBookRepository.findAllByUuid(uuid).collectList().awaitFirstOrElse { emptyList() }
    }

    override suspend fun update(request: AddressBook) {
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
            addressBookRepository.save(addressBook).awaitFirst()
        } else throw OpexError.Forbidden.exception()
    }

    override suspend fun delete(uuid: String, id: Long) {
        val addressBook =
            addressBookRepository.findById(id).awaitFirstOrNull() ?: throw OpexError.AddressBookNotFound.exception()
        if (addressBook.uuid == uuid)
            addressBookRepository.deleteById(id).awaitFirstOrNull()
        else throw OpexError.Forbidden.exception()
    }
}