package co.nilin.opex.port.bcgateway.postgres.dao

import co.nilin.opex.port.bcgateway.postgres.model.WalletSyncScheduleModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface WalletSyncScheduleRepository : ReactiveCrudRepository<WalletSyncScheduleModel, Long>