package co.nilin.opex.otp.app.service;

import co.nilin.opex.otp.app.data.SMSProviderType;
import co.nilin.opex.otp.app.proxy.SMSProvider;
import co.nilin.opex.otp.app.repository.SMSProviderRouteRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component;

@Component
class SMSProviderRouter(
    providers: List<SMSProvider>,
    private val routeRepository: SMSProviderRouteRepository,
    @Value("\${otp.sms.provider.default}")
    private val defaultSmsProvider: SMSProviderType?,
) {

    private val providerMap: Map<SMSProviderType, SMSProvider> = providers.associateBy { it.type }

    suspend fun getProvider(receiver: String): SMSProvider {
        val routes = routeRepository.findAllByEnabledTrue()

        val providerType = routes
            .sortedByDescending { it.prefix.length }
            .firstOrNull { receiver.startsWith(it.prefix) }
            ?.provider
            ?.let(SMSProviderType::valueOf)
            ?: defaultSmsProvider

        return providerMap[providerType]
            ?: throw IllegalStateException(
                "SMS provider $providerType is configured but no implementation was found."
            )
    }
}