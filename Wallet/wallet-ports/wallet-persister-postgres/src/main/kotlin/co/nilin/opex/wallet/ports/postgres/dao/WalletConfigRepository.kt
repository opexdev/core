package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.WalletConfigModel
import co.nilin.opex.wallet.ports.postgres.model.WalletModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface WalletConfigRepository : ReactiveCrudRepository<WalletConfigModel, String>