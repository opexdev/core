package co.nilin.opex.otp.app.repository

import co.nilin.opex.otp.app.model.TOTP
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TOTPRepository : CoroutineCrudRepository<TOTP, Long> {

    suspend fun findByUserId(userId: String): TOTP?

    @Query("update totp set is_activated = true where id = :id")
    suspend fun markActivated(id:Long)
}