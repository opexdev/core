package co.nilin.opex.port.bcgateway.postgres.dao

import co.nilin.opex.port.bcgateway.postgres.model.ChainSyncDepositModel
import co.nilin.opex.port.bcgateway.postgres.model.WalletSyncDepositModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface WalletSyncDepositRepository : ReactiveCrudRepository<WalletSyncDepositModel, Long>
