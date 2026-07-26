package co.nilin.opex.bcgateway.ports.postgres.impl

import co.nilin.opex.bcgateway.core.model.AddressType
import co.nilin.opex.bcgateway.core.spi.AddressTypeHandler
import co.nilin.opex.bcgateway.ports.postgres.dao.AddressTypeRepository
import co.nilin.opex.bcgateway.ports.postgres.model.AddressTypeModel
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

@Component
class AddressTypeHandlerImpl(private val repository: AddressTypeRepository) : AddressTypeHandler {

    override suspend fun fetchAll(): List<AddressType> {
        return repository.findAll()
            .collectList()
            .awaitFirstOrElse { emptyList() }
            .map { AddressType(it.id!!, it.type, it.addressRegex, it.memoRegex) }
    }

    override suspend fun addAddressType(name: String, addressRegex: String, memoRegex: String?) {
        if (repository.findByType(name).awaitFirstOrNull() == null) {
            repository.save(AddressTypeModel(null, name, addressRegex, memoRegex)).awaitFirstOrNull()
        }
    }

    override suspend fun fetchAddressType(name: String): AddressType? {
        return repository.findByType(name)
            .map { AddressType(it.id!!, it.type, it.addressRegex, it.memoRegex) }.awaitFirstOrNull()
    }
}