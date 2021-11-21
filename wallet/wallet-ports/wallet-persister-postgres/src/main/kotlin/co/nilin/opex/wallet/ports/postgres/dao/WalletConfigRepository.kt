package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.WalletConfigModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface WalletConfigRepository : ReactiveCrudRepository<WalletConfigModel, String>