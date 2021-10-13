package co.nilin.opex.port.bcgateway.postgres.impl

import co.nilin.opex.bcgateway.core.model.AddressType
import co.nilin.opex.bcgateway.core.model.Chain
import co.nilin.opex.bcgateway.core.model.Endpoint
import co.nilin.opex.bcgateway.core.spi.ChainLoader
import co.nilin.opex.port.bcgateway.postgres.dao.ChainRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.stereotype.Component

@Component
class ChainHandler(val chainRepository: ChainRepository) : ChainLoader {
    override suspend fun fetchChainInfo(chain: String): Chain {
        val chainDao = chainRepository.findByName(chain).awaitSingle()
        val addressTypes = chainRepository.findAddressTypesByName(chain)
            .map { AddressType(it.id!!, it.type, it.addressRegex, it.memoRegex) }.toList()
        val endpoints = chainRepository.findEndpointsByName(chain).map { Endpoint(it.url) }.toList()
        return Chain(chainDao.name, addressTypes, endpoints)
    }
}
