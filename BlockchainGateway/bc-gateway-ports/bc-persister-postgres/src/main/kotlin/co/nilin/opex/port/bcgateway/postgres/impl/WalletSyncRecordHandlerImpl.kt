package co.nilin.opex.port.bcgateway.postgres.impl

import co.nilin.opex.bcgateway.core.model.Deposit
import co.nilin.opex.bcgateway.core.model.WalletSyncSchedule
import co.nilin.opex.bcgateway.core.spi.WalletSyncRecordHandler
import co.nilin.opex.bcgateway.core.spi.WalletSyncSchedulerHandler
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class WalletSyncRecordHandlerImpl : WalletSyncRecordHandler {
    override suspend fun saveReadyToSyncTransfers(chainName: String, deposits: List<Deposit>) {
        TODO("Not yet implemented")
    }

    override suspend fun findReadyToSyncTransfers(count: Long?): List<Deposit> {
        TODO("Not yet implemented")
    }
}