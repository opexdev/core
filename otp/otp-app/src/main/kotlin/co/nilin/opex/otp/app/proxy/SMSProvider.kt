package co.nilin.opex.otp.app.proxy

import co.nilin.opex.otp.app.data.SMSProviderType

interface SMSProvider {

    val type: SMSProviderType

    suspend fun send(
        receiver: String,
        message: String,
    ): Boolean
}