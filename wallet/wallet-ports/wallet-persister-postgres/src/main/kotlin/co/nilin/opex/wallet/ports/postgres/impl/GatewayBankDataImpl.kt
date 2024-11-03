package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.inout.BankDataCommand
import co.nilin.opex.wallet.core.spi.CurrencyServiceManager
import co.nilin.opex.wallet.core.spi.GatewayBankDataManager
import co.nilin.opex.wallet.ports.postgres.dao.BankDataRepository
import co.nilin.opex.wallet.ports.postgres.dao.GatewayBankDataRepository
import co.nilin.opex.wallet.ports.postgres.dao.OffChainGatewayRepository
import co.nilin.opex.wallet.ports.postgres.model.GatewayBankDataModel
import co.nilin.opex.wallet.ports.postgres.util.toDto
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.collect
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.Collections

@Component
class GatewayBankDataImpl(
    private val gatewayRepository: OffChainGatewayRepository,
    private val gatewayBankDataRepository: GatewayBankDataRepository,
    private val bankDataRepository: BankDataRepository
) : GatewayBankDataManager {
    private val logger = LoggerFactory.getLogger(GatewayBankDataImpl::class.java)

    override suspend fun assignBankDataToGateway(gatewayUuid: String, bankData: List<String>) {
        gatewayRepository.findByGatewayUuid(gatewayUuid)?.awaitSingleOrNull()?.let { gateway ->
            bankData.forEach { it ->
                bankDataRepository.findByUuid(
                    it
                )?.awaitSingleOrNull()?.let {
                    runCatching {
                        gatewayBankDataRepository.save(GatewayBankDataModel(null, it.id!!, gateway.id!!))
                            ?.awaitSingleOrNull()
                    }

                }
            }

        } ?: throw OpexError.GatewayNotFount.exception()
    }

    override suspend fun getAssignedBankDataToGateway(gatewayUuid: String): List<BankDataCommand>? {
        return gatewayRepository.findByGatewayUuid(gatewayUuid)?.awaitSingleOrNull()?.let { gateway ->
            gatewayBankDataRepository.findByGatewayId(gateway.id!!)?.map { it.toDto() }?.collectList()
                ?.awaitSingleOrNull()
        } ?: throw OpexError.GatewayNotFount.exception()
    }

    override suspend fun revokeBankDataToGateway(gatewayUuid: String, bankData: List<String>) {
        gatewayRepository.findByGatewayUuid(gatewayUuid)?.awaitSingleOrNull()?.let { gateway ->
            bankData.forEach { it ->
                bankDataRepository.findByUuid(
                    it
                )?.awaitSingleOrNull()?.let {
                    runCatching {
                        gatewayBankDataRepository.deleteByBankDataIdAndGatewayId(it.id!!, gateway.id!!)
                            ?.awaitSingleOrNull()
                    }

                }
            }

        } ?: throw OpexError.GatewayNotFount.exception()
    }

}