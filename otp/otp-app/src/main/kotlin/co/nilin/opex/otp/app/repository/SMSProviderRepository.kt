package co.nilin.opex.otp.app.repository

import co.nilin.opex.otp.app.model.SMSProvider
import co.nilin.opex.otp.app.model.SMSProviderRoute
import co.nilin.opex.otp.app.model.TOTPConfig
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SMSProviderRepository : CoroutineCrudRepository<SMSProvider, String> {
    @Query("select * from sms_provider where id=:type")
    suspend fun findConfig(type: String): SMSProvider
}