package co.nilin.mixchange.port.wallet.postgres.dao

import co.nilin.mixchange.port.wallet.postgres.model.WalletConfigModel
import co.nilin.mixchange.port.wallet.postgres.model.WalletModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface WalletConfigRepository : ReactiveCrudRepository<WalletConfigModel, String>