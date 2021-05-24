package co.nilin.mixchange.port.wallet.postgres.dao

import co.nilin.mixchange.port.wallet.postgres.model.UserLimitsModel
import co.nilin.mixchange.port.wallet.postgres.model.WalletLimitsModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UserLimitsRepository : ReactiveCrudRepository<UserLimitsModel, Long> {
    @Query("select * from user_limits where level = :level and action = :action and owner is null")
    fun findByLevelAndAction(
        @Param("level") level: String,
        @Param("action") action: String
    ): Flow<UserLimitsModel?>

    @Query("select * from user_limits where owner = :owner and action = :action")
    fun findByOwnerAndAction(
        @Param("owner") owner: Long,
        @Param("action") action: String
    ): Flow<UserLimitsModel?>

}