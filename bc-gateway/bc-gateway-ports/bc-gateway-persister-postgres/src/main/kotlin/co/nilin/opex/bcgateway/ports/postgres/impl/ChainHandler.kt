package co.nilin.opex.bcgateway.ports.postgres.impl

import co.nilin.opex.bcgateway.core.model.AddressType
import co.nilin.opex.bcgateway.core.model.Chain
import co.nilin.opex.bcgateway.core.spi.ChainLoader
import co.nilin.opex.bcgateway.ports.postgres.dao.AddressTypeRepository
import co.nilin.opex.bcgateway.ports.postgres.dao.ChainAddressTypeRepository
import co.nilin.opex.bcgateway.ports.postgres.dao.ChainRepository
import co.nilin.opex.bcgateway.ports.postgres.model.ChainAddressTypeModel
import co.nilin.opex.common.OpexError
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.stereotype.Component

@Component
class ChainHandler(
    private val chainRepository: ChainRepository,
    private val addressTypeRepository: AddressTypeRepository,
    private val chainAddressRepository: ChainAddressTypeRepository
) : ChainLoader {

    override suspend fun addChain(name: String, addressType: String): Chain {
        val chain = chainRepository.findByName(name)?.awaitFirstOrNull()
        if (chain != null)
            throw OpexError.BadRequest.exception()

        val type = addressTypeRepository.findByType(addressType).awaitFirstOrNull()
            ?: throw OpexError.InvalidAddressType.exception()

        chainRepository.insert(name).awaitFirstOrNull()
        val model = chainRepository.findByName(name).awaitFirst()
        chainAddressRepository.save(ChainAddressTypeModel(null, model.name, type.id!!)).awaitFirstOrNull()
        return Chain(model.name, emptyList())
    }

    override suspend fun fetchAllChains(): List<Chain> {
        return chainRepository.findAll()
            .collectList()
            .awaitFirstOrElse { emptyList() }
            .map { c ->
                val addressTypes = chainRepository.findAddressTypesByName(c.name)
                    .map { AddressType(it.id!!, it.type, it.addressRegex, it.memoRegex) }
                    .toList()

                Chain(c.name, addressTypes)
            }
    }

    override suspend fun fetchChainInfo(chain: String): Chain {
        val chainDao = chainRepository.findByName(chain)?.awaitSingle()
        val addressTypes = chainRepository.findAddressTypesByName(chain)
            .map { AddressType(it.id!!, it.type, it.addressRegex, it.memoRegex) }.toList()
        return Chain(chainDao.name, addressTypes)
    }

}
