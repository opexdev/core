package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.BankDataCommand

interface GatewayBankDataManager {
    suspend fun assignBankDataToGateway(gatewayUuid: String, bankData: List<String>)

    suspend fun getAssignedBankDataToGateway(gatewayUuid: String): List<BankDataCommand>?

    suspend fun revokeBankDataToGateway(gatewayUuid: String, bankData: List<String>)

}