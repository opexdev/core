package co.nilin.opex.port.bcgateway.postgres.impl

import co.nilin.opex.bcgateway.core.model.Chain
import co.nilin.opex.bcgateway.core.spi.ChainLoader
import co.nilin.opex.port.bcgateway.postgres.dao.ChainRepository
import org.springframework.stereotype.Component

@Component
class ChainHandler(val chainRepository: ChainRepository): ChainLoader {
    override suspend fun fetchChainInfo(chain: String): Chain {
        TODO("Not implemented!")
    }
}