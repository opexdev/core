package co.nilin.opex.bcgateway.ports.postgres.impl

import co.nilin.opex.bcgateway.core.model.*
import co.nilin.opex.bcgateway.core.spi.CryptoCurrencyHandler
import co.nilin.opex.bcgateway.core.spi.CryptoCurrencyHandlerV2
import co.nilin.opex.bcgateway.ports.postgres.dao.ChainRepository
import co.nilin.opex.bcgateway.ports.postgres.dao.CurrencyImplementationRepository
import co.nilin.opex.bcgateway.ports.postgres.dao.CurrencyRepository
import co.nilin.opex.bcgateway.ports.postgres.dao.NewCurrencyImplementationRepository
import co.nilin.opex.bcgateway.ports.postgres.model.CurrencyImplementationModel
import co.nilin.opex.bcgateway.ports.postgres.model.CurrencyModel
import co.nilin.opex.bcgateway.ports.postgres.model.NewCurrencyImplementationModel
import co.nilin.opex.bcgateway.ports.postgres.util.toDto
import co.nilin.opex.bcgateway.ports.postgres.util.toModel
import co.nilin.opex.common.OpexError
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class CurrencyHandlerImplV2(
        private val chainRepository: ChainRepository,
        private val currencyImplementationRepository: NewCurrencyImplementationRepository
) : CryptoCurrencyHandlerV2 {

    private val logger = LoggerFactory.getLogger(CryptoCurrencyHandler::class.java)
    override suspend fun createImpl(request: CryptoCurrencyCommand) {
        doSave(request.toModel());
    }

    override suspend fun updateImpl(request: CryptoCurrencyCommand):CryptoCurrencyCommand? {
       return fetchImpl(request.currencyImpUuid)?.let {
            doSave(it.toDto().updateTo(request).toModel().apply { id = it.id })?.toDto()

        }?:throw OpexError.CurrencyNotFound.exception()

    }

    override suspend fun fetchCurrencyImpls(currencyUuid: String) {
        TODO("Not yet implemented")
    }

    override suspend fun fetchImpls(request: String) {
        TODO("Not yet implemented")
    }

    override suspend fun fetchImpl(request: String) {
        return currencyImplementationRepository.findByCurrencyImplUuid(request)?.awaitFirstOrElse {
            throw OpexError.CurrencyNotFound.exception()
        }    }

    private suspend fun loadImpl(uuid: String): NewCurrencyImplementationModel? {
        return currencyImplementationRepository.findByCurrencyImplUuid(uuid)?.awaitFirstOrElse {
            throw OpexError.CurrencyNotFound.exception()
        }

    }


    private suspend fun doSave(request: NewCurrencyImplementationModel): NewCurrencyImplementationModel? {
        return currencyImplementationRepository.save(request).awaitSingleOrNull()
    }

}
