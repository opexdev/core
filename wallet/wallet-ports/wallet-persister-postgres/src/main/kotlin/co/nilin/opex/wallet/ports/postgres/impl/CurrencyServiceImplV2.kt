package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.common.data.UserLanguage
import co.nilin.opex.common.utils.LanguageUtils.getDefaultUserLanguage
import co.nilin.opex.common.utils.LanguageUtils.getUserLanguage
import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.model.FetchCurrency
import co.nilin.opex.wallet.core.spi.CurrencyServiceManager
import co.nilin.opex.wallet.ports.postgres.dao.CurrencyLocalizationsRepository
import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepositoryV2
import co.nilin.opex.wallet.ports.postgres.dto.CurrencyView
import co.nilin.opex.wallet.ports.postgres.model.CurrencyLocalizationModel
import co.nilin.opex.wallet.ports.postgres.model.CurrencyModel
import co.nilin.opex.wallet.ports.postgres.util.RedisCacheHelper
import co.nilin.opex.wallet.ports.postgres.util.toCommand
import co.nilin.opex.wallet.ports.postgres.util.toCurrencyData
import co.nilin.opex.wallet.ports.postgres.util.toModel
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.util.stream.Collectors

@Service("newVersion")
class CurrencyServiceImplV2(
    val currencyRepository: CurrencyRepositoryV2,
    val redisCacheHelper: RedisCacheHelper,
    val currencyLocalizationsRepository: CurrencyLocalizationsRepository,
    val transactionalOperator: TransactionalOperator
) : CurrencyServiceManager {
    private val logger = LoggerFactory.getLogger(CurrencyServiceImplV2::class.java)


    override suspend fun createNewCurrency(request: CurrencyCommand, ignoreIfExist: Boolean?): CurrencyCommand? {
        return loadCurrency(FetchCurrency(symbol = request.symbol))?.awaitFirstOrNull()?.let {
            if (!ignoreIfExist!!) throw OpexError.CurrencyIsExist.exception()
            else return null
        } ?: run {
            return doPersist(request)?.toCommand()
                .also { redisCacheHelper.put("${request.symbol}-precision", request.precision) }
        }
    }


    override suspend fun updateCurrency(request: CurrencyCommand): CurrencyCommand? {
        return loadCurrency(FetchCurrency(symbol = request.symbol))?.awaitFirstOrNull()?.let {
            doSave(it.toCommand().updateTo(request).toModel())?.toCommand()
                .also { redisCacheHelper.put("${request.symbol}-precision", request.precision) }
        } ?: throw OpexError.CurrencyNotFound.exception()

    }

    override suspend fun deleteCurrency(request: FetchCurrency): Void? {
        return loadCurrency(request)?.awaitFirstOrNull()?.let {
            currencyRepository.deleteById(it.symbol!!)?.awaitFirstOrNull()
                .also { redisCacheHelper.evict("${request.symbol}-precision") }
        }
    }

    override suspend fun fetchCurrencies(request: FetchCurrency?): CurrenciesCommand? {
        return CurrenciesCommand(
            loadCurrencies(request)?.map { it.toCommand() }?.collect(Collectors.toList())
                ?.awaitFirstOrNull()
        )
    }

    override suspend fun fetchAllCurrencies(): List<CurrencyData> {
        return currencyRepository.fetchAll(UserLanguage.safeValueOf(getUserLanguage().awaitSingleOrNull()).toString())
            .map {
                it.toCurrencyData()
            }.collectList().awaitFirstOrElse { emptyList() }
    }

    override suspend fun fetchCurrency(request: FetchCurrency): CurrencyCommand? {
        return loadCurrency(request)?.awaitFirstOrNull()?.toCommand()
    }

    override suspend fun fetchAllCurrenciesPrecision(): List<CurrencyPrecision> {
        return currencyRepository.fetchAllCurrenciesPrecision().collectList().awaitFirstOrElse { emptyList() }
    }

    override suspend fun fetchCurrencyMaxOrder(symbol: String): BigDecimal? {
        return currencyRepository.fetchCurrencyMaxOrder(symbol)?.awaitFirstOrNull()
    }

    override suspend fun fetchCurrencyLocalizations(symbol: String): List<CurrencyLocalizationCommand> {
        loadCurrency(FetchCurrency(symbol = symbol))?.awaitFirstOrNull()?.let {
            return currencyLocalizationsRepository.findByCurrency(symbol).map { it.toCommand() }.collectList()
                .awaitSingleOrNull() ?: emptyList()
        } ?: throw OpexError.CurrencyNotFound.exception()
    }

    override suspend fun saveCurrencyLocalizations(
        symbol: String, localizations: List<CurrencyLocalizationCommand>
    ): List<CurrencyLocalizationCommand> {

        return transactionalOperator.executeAndAwait {

            loadCurrency(FetchCurrency(symbol = symbol))?.awaitFirstOrNull()
                ?: throw OpexError.CurrencyNotFound.exception()

            localizations.forEach { c ->
                currencyLocalizationsRepository.save(
                    CurrencyLocalizationModel(
                        currency = symbol,
                        name = c.name,
                        title = c.title,
                        alias = c.alias,
                        description = c.description,
                        shortDescription = c.shortDescription,
                        language = UserLanguage.safeValueOf(c.language).toString()
                    )
                ).awaitSingle()
            }

            currencyLocalizationsRepository.findByCurrency(symbol).map { it.toCommand() }.collectList()
                .awaitSingleOrNull() ?: emptyList()
        } ?: throw OpexError.BadRequest.exception("Failed to save currency localizations")
    }

    override suspend fun updateCurrencyLocalization(request: CurrencyLocalizationCommand): CurrencyLocalizationCommand {
        if (request.id != null) {
            val localizationModel =
                currencyLocalizationsRepository.findById(request.id!!).awaitSingleOrNull()
                    ?: throw OpexError.CurrencyLocalizationNotFound.exception()
            localizationModel.apply {
                name = request.name
                title = request.title
                alias = request.alias
                description = request.description
                shortDescription = request.shortDescription
            }
            return currencyLocalizationsRepository.save(localizationModel).awaitSingle().toCommand()
        }
        throw OpexError.CurrencyLocalizationNotFound.exception()
    }

    override suspend fun deleteCurrencyLocalization(id: Long) {
        currencyLocalizationsRepository.deleteById(id).awaitSingleOrNull()
    }

    private suspend fun loadCurrency(request: FetchCurrency): Mono<CurrencyView>? {
        if (request.uuid == null && request.symbol == null) throw OpexError.BadRequest.exception()
        return currencyRepository.fetchCurrency(
            symbol = request.symbol,
            uuid = request.uuid,
            lang = UserLanguage.safeValueOf(getUserLanguage().awaitSingleOrNull()).toString()
        )
    }

    private suspend fun loadCurrencies(request: FetchCurrency?): Flux<CurrencyView>? {
        return currencyRepository.fetchSemiCurrencies(
            request?.symbol, request?.name, UserLanguage.safeValueOf(getUserLanguage().awaitSingleOrNull()).toString()
        )
    }

    private suspend fun doSave(request: CurrencyModel): CurrencyView? {
        currencyRepository.save(request).awaitFirstOrNull()
        return currencyRepository.fetchCurrency(
            uuid = request.uuid, lang = getDefaultUserLanguage()
        )?.awaitFirstOrNull()
    }

    private suspend fun doPersist(request: CurrencyCommand): CurrencyView? {
        return transactionalOperator.executeAndAwait {

            currencyRepository.insert(
                request.symbol,
                request.uuid!!,
                request.precision,
                request.icon,
                request.isTransitive,
                request.isActive,
                request.sign,
                request.externalUrl,
                request.displayOrder
            ).awaitFirstOrNull()

            if (listOf(
                    request.name,
                    request.title,
                    request.alias,
                    request.description,
                    request.shortDescription
                ).any { !it.isNullOrBlank() }
            ) {
                currencyLocalizationsRepository.save(
                    CurrencyLocalizationModel(
                        currency = request.symbol,
                        name = request.name,
                        title = request.title,
                        alias = request.alias,
                        description = request.description,
                        shortDescription = request.shortDescription,
                        language = getDefaultUserLanguage()
                    )
                ).awaitFirstOrNull()
            }

            currencyRepository.fetchCurrency(
                uuid = request.uuid, lang = getDefaultUserLanguage()
            )?.awaitFirstOrNull()
        }
    }


}