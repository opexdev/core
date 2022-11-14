package co.nilin.opex.accountant.ports.postgres.dao

import co.nilin.opex.accountant.ports.postgres.model.UserLevelModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UserLevelRepository : ReactiveCrudRepository<UserLevelModel, String> {

    @Query("insert into user_level (level) values (:level) on conflict do nothing")
    fun insert(level: String): Mono<Void>

}