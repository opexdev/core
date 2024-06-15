//package co.nilin.opex.bcgateway.ports.postgres.impl
//
//import co.nilin.opex.bcgateway.core.model.*
//import co.nilin.opex.bcgateway.core.spi.CryptoCurrencyHandler
//import co.nilin.opex.bcgateway.ports.postgres.dao.ChainRepository
//import co.nilin.opex.bcgateway.ports.postgres.dao.CurrencyImplementationRepository
//import co.nilin.opex.bcgateway.ports.postgres.dao.CurrencyRepository
//import co.nilin.opex.bcgateway.ports.postgres.model.CurrencyImplementationModel
//import co.nilin.opex.bcgateway.ports.postgres.model.CurrencyModel
//import co.nilin.opex.common.OpexError
//import kotlinx.coroutines.flow.map
//import kotlinx.coroutines.flow.toList
//import kotlinx.coroutines.reactive.awaitFirst
//import kotlinx.coroutines.reactive.awaitFirstOrElse
//import kotlinx.coroutines.reactive.awaitFirstOrNull
//import kotlinx.coroutines.reactive.awaitSingle
//import kotlinx.coroutines.reactor.awaitSingleOrNull
//import org.slf4j.LoggerFactory
//import org.springframework.stereotype.Component
//import java.math.BigDecimal
//
//@Component
//class CurrencyHandlerImpl(
//    private val chainRepository: ChainRepository,
//    private val currencyRepository: CurrencyRepository,
//    private val currencyImplementationRepository: CurrencyImplementationRepository
//) : CryptoCurrencyHandler {
//
//    private val logger = LoggerFactory.getLogger(CryptoCurrencyHandler::class.java)
//
//    override suspend fun addCurrency(name: String, symbol: String) {
//        try {
//            currencyRepository.insert(name, symbol.uppercase()).awaitSingleOrNull()
//        } catch (e: Exception) {
//            logger.error("Could not insert new currency $name", e)
//        }
//    }
//
//    override suspend fun addCurrencyImplementationV2(
//        currencySymbol: String,
//        implementationSymbol: String,
//        currencyName: String,
//        chain: String,
//        tokenName: String?,
//        tokenAddress: String?,
//        isToken: Boolean,
//        withdrawFee: BigDecimal,
//        minimumWithdraw: BigDecimal,
//        isWithdrawEnabled: Boolean,
//        decimal: Int
//    ): CurrencyImplementation {
//        currencyRepository.findBySymbol(currencySymbol).awaitFirstOrNull()?.let {
//            throw OpexError.CurrencyIsExist.exception()
//        } ?: run {
//            addCurrency(currencyName, currencySymbol)
//            return addCurrencyImplementation(
//                currencySymbol,
//                implementationSymbol,
//                chain,
//                tokenName,
//                tokenAddress,
//                isToken,
//                withdrawFee,
//                minimumWithdraw,
//                isWithdrawEnabled,
//                decimal
//            )
//        }
//    }
//
//    override suspend fun updateCurrencyImplementation(
//        currencySymbol: String,
//        implementationSymbol: String,
//        currencyName: String,
//        newChain: String?,
//        tokenName: String?,
//        tokenAddress: String?,
//        isToken: Boolean,
//        withdrawFee: BigDecimal,
//        minimumWithdraw: BigDecimal,
//        isWithdrawEnabled: Boolean,
//        decimal: Int,
//        oldChain: String
//    ): CurrencyImplementation? {
//        currencyRepository.findBySymbol(currencySymbol).awaitFirstOrNull()?.let {cm->
//            currencyRepository.save(CurrencyModel(currencySymbol, currencyName)).awaitSingleOrNull()
//            return currencyImplementationRepository.findByCurrencySymbolAndChain(currencySymbol, oldChain)
//                ?.awaitSingleOrNull()
//                ?.let {
//                    it.apply {
//                        this.implementationSymbol = implementationSymbol
//                        this.chain = newChain?:oldChain
//                        this.decimal = decimal
//                        this.token = isToken
//                        this.tokenAddress = tokenAddress
//                        this.tokenName = tokenName
//                        this.withdrawEnabled = isWithdrawEnabled
//                        this.withdrawFee = withdrawFee
//                        this.withdrawMin = minimumWithdraw
//                    }
//                    currencyImplementationRepository.save(it).awaitSingleOrNull()?.let {icm-> projectCurrencyImplementation(icm, cm) }
//                }
//
//        } ?: throw OpexError.CurrencyNotFound.exception()
//    }
//
//    override suspend fun editCurrency(name: String, symbol: String) {
//        val currency = currencyRepository.findBySymbol(symbol).awaitFirstOrNull()
//        if (currency != null) {
//            currency.name = name
//            currencyRepository.save(currency).awaitFirst()
//        }
//    }
//
//    override suspend fun deleteCurrency(name: String) {
//        try {
//            currencyRepository.deleteByName(name).awaitFirstOrNull()
//        } catch (e: Exception) {
//            logger.error("Could not delete currency $name", e)
//        }
//    }
//
//    override suspend fun addCurrencyImplementation(
//        currencySymbol: String,
//        implementationSymbol: String,
//        chain: String,
//        tokenName: String?,
//        tokenAddress: String?,
//        isToken: Boolean,
//        withdrawFee: BigDecimal,
//        minimumWithdraw: BigDecimal,
//        isWithdrawEnabled: Boolean,
//        decimal: Int
//    ): CurrencyImplementation {
//        val chainModel = chainRepository.findByName(chain).awaitFirstOrNull()
//            ?: throw OpexError.ChainNotFound.exception()
//
//        currencyImplementationRepository.findByCurrencySymbolAndChain(currencySymbol.uppercase(), chain)
//            .awaitFirstOrNull()
//            ?.let { throw OpexError.DuplicateToken.exception() }
//
//        val currency = currencyRepository.findBySymbol(currencySymbol.uppercase()).awaitFirstOrNull()
//            ?: throw OpexError.CurrencyNotFoundBC.exception()
//
//        val model = currencyImplementationRepository.save(
//            CurrencyImplementationModel(
//                null,
//                currencySymbol.uppercase(),
//                implementationSymbol,
//                chainModel.name,
//                isToken,
//                tokenAddress,
//                tokenName,
//                isWithdrawEnabled,
//                withdrawFee,
//                minimumWithdraw,
//                decimal
//            )
//        ).awaitFirst()
//
//        logger.info("Add currency implementation: ${model.currencySymbol} - ${model.chain}")
//
//        return projectCurrencyImplementation(model, currency)
//    }
//
//    override suspend fun fetchAllImplementations(): List<CurrencyImplementation> {
//        return currencyImplementationRepository.findAll()
//            .collectList()
//            .awaitFirstOrElse { emptyList() }
//            .map {
//                val currency = currencyRepository.findBySymbol(it.currencySymbol).awaitFirstOrNull()
//                projectCurrencyImplementation(it, currency)
//            }
//    }
//
//    override suspend fun fetchCurrencyInfo(symbol: String): CurrencyInfo {
//        val symbolUpperCase = symbol.uppercase()
//        val currencyModel = currencyRepository.findBySymbol(symbolUpperCase).awaitSingleOrNull()
//        if (currencyModel === null) {
//            return CurrencyInfo(Currency("", symbolUpperCase), emptyList())
//        }
//        val currencyImplModel = currencyImplementationRepository.findByCurrencySymbol(symbolUpperCase)
//        val currency = Currency(currencyModel.symbol, currencyModel.name)
//        val implementations = currencyImplModel.map { projectCurrencyImplementation(it, currencyModel) }
//        return CurrencyInfo(currency, implementations.toList())
//    }
//
//    override suspend fun findByChainAndTokenAddress(chain: String, address: String?): CurrencyImplementation? {
//        val impl = currencyImplementationRepository.findByChainAndTokenAddress(chain, address)
//            .awaitFirstOrNull()
//
//        return if (impl != null)
//            projectCurrencyImplementation(impl)
//        else
//            null
//    }
//
//    override suspend fun findImplementationsWithTokenOnChain(chain: String): List<CurrencyImplementation> {
//        return currencyImplementationRepository.findByChain(chain).map { projectCurrencyImplementation(it) }.toList()
//    }
//
//    override suspend fun findImplementationsByCurrency(currency: String): List<CurrencyImplementation> {
//        return currencyImplementationRepository.findByCurrencySymbol(currency)
//            .map { projectCurrencyImplementation(it) }
//            .toList()
//    }
//
//    override suspend fun changeWithdrawStatus(symbol: String, chain: String, status: Boolean) {
//        val impl = currencyImplementationRepository.findByCurrencySymbolAndChain(symbol, chain).awaitSingleOrNull()
//            ?: throw OpexError.TokenNotFound.exception()
//
//        impl.apply {
//            withdrawEnabled = status
//            currencyImplementationRepository.save(impl).awaitFirstOrNull()
//        }
//    }
//
//    private suspend fun projectCurrencyImplementation(
//        currencyImplementationModel: CurrencyImplementationModel,
//        currencyModel: CurrencyModel? = null
//    ): CurrencyImplementation {
//        val addressTypesModel = chainRepository.findAddressTypesByName(currencyImplementationModel.chain)
//        val addressTypes =
//            addressTypesModel.map { AddressType(it.id!!, it.type, it.addressRegex, it.memoRegex) }.toList()
//        val currencyModelVal =
//            currencyModel ?: currencyRepository.findBySymbol(currencyImplementationModel.currencySymbol).awaitSingle()
//        return CurrencyImplementation(
//            Currency(currencyModelVal.symbol, currencyModelVal.name),
//            Currency(currencyImplementationModel.implementationSymbol, currencyModelVal.name),
//            Chain(currencyImplementationModel.chain, addressTypes),
//            currencyImplementationModel.token,
//            currencyImplementationModel.tokenAddress,
//            currencyImplementationModel.tokenName,
//            currencyImplementationModel.withdrawEnabled,
//            currencyImplementationModel.withdrawFee,
//            currencyImplementationModel.withdrawMin,
//            currencyImplementationModel.decimal
//        )
//    }
//}
