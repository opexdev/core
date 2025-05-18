package co.nilin.opex.otp.app.repository

import co.nilin.opex.otp.app.model.TOTPConfig
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TOTPConfigRepository : CoroutineCrudRepository<TOTPConfig, Boolean> {

    @Query("select * from totp_config limit 1")
    suspend fun findOne(): TOTPConfig
}