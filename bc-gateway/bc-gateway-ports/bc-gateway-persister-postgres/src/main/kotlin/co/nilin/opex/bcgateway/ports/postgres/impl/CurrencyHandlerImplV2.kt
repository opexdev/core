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
import reactor.core.publisher.Flux
import java.math.BigDecimal
import java.util.stream.Collectors

@Component
class CurrencyHandlerImplV2(
        private val chainRepository: ChainRepository,
        private val currencyImplementationRepository: NewCurrencyImplementationRepository
) : CryptoCurrencyHandlerV2 {

    private val logger = LoggerFactory.getLogger(CryptoCurrencyHandler::class.java)
    override suspend fun createImpl(request: CryptoCurrencyCommand): CryptoCurrencyCommand? {
        return doSave(request.toModel())?.toDto();
    }

    override suspend fun updateImpl(request: CryptoCurrencyCommand): CryptoCurrencyCommand? {
        return loadImpls(FetchImpls(implUuid = request.currencyImpUuid))?.awaitFirstOrElse { throw OpexError.CurrencyNotFound.exception() }?.let {
            doSave(it.toDto().updateTo(request).toModel().apply { id = it.id })?.toDto()
        }

    }

    override suspend fun fetchCurrencyImpls(currencyUuid: String): CurrencyImps? {

        return CurrencyImps(loadImpls(FetchImpls(currencyUuid = currencyUuid))?.toStream()
                ?.map { it.toDto() }?.collect(Collectors.toList()))
    }

    override suspend fun fetchCurrencyImpls(data: FetchImpls): CurrencyImps? {
        return CurrencyImps(loadImpls(data)?.toStream()
                ?.map { it.toDto() }?.collect(Collectors.toList()))    }

    override suspend fun fetchImpls(): CurrencyImps? {
        return CurrencyImps(loadImpls(FetchImpls())?.toStream()?.map { it.toDto() }?.collect(Collectors.toList()))
    }

    override suspend fun fetchImpl(request: String): CryptoCurrencyCommand? {
        return loadImpls(FetchImpls(implUuid = request))?.awaitFirstOrElse {
            throw OpexError.CurrencyNotFound.exception()
        }?.toDto()
    }

    private suspend fun loadImpls(request: FetchImpls): Flux<NewCurrencyImplementationModel>? {
        return currencyImplementationRepository.findImpls(request.currencyUuid, request.implUuid, request.chain, request.currencyImplementationName)? {
            throw OpexError.CurrencyNotFound.exception()
        }
    }

    private suspend fun doSave(request: NewCurrencyImplementationModel): NewCurrencyImplementationModel? {
        return currencyImplementationRepository.save(request).awaitSingleOrNull()
    }

}
