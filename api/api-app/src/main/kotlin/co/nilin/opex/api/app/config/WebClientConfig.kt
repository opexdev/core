package co.nilin.opex.api.app.config

import co.nilin.opex.common.utils.LanguageUtils.getDefaultUserLanguage
import io.netty.channel.ChannelOption
import io.netty.handler.logging.LogLevel
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.zalando.logbook.Logbook
import org.zalando.logbook.netty.LogbookClientHandler
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import reactor.netty.transport.logging.AdvancedByteBufFormat
import java.time.Duration

@Configuration
class WebClientConfig(
    private val logbook: Logbook,
    @Value("\${app.auth.url}")
    private val url: String,
    @Value("\${app.http.client.wiretap.enabled:false}")
    private val wiretapEnabled: Boolean,
    @Value("\${app.http.client.general.max-connections:300}")
    private val generalMaxConnections: Int,
    @Value("\${app.http.client.general.pending-acquire-max-count:1000}")
    private val generalPendingAcquireMaxCount: Int,
    @Value("\${app.http.client.general.max-idle-seconds:30}")
    private val generalMaxIdleSeconds: Long,
    @Value("\${app.http.client.general.max-life-seconds:120}")
    private val generalMaxLifeSeconds: Long,
    @Value("\${app.http.client.general.pending-acquire-timeout-seconds:30}")
    private val generalPendingAcquireTimeoutSeconds: Long,
    @Value("\${app.http.client.general.connect-timeout-millis:5000}")
    private val generalConnectTimeoutMillis: Int,
    @Value("\${app.http.client.general.response-timeout-seconds:30}")
    private val generalResponseTimeoutSeconds: Long,
    @Value("\${app.http.client.keycloak.max-connections:150}")
    private val keycloakMaxConnections: Int,
    @Value("\${app.http.client.keycloak.pending-acquire-max-count:500}")
    private val keycloakPendingAcquireMaxCount: Int,
    @Value("\${app.http.client.keycloak.max-idle-seconds:30}")
    private val keycloakMaxIdleSeconds: Long,
    @Value("\${app.http.client.keycloak.max-life-seconds:120}")
    private val keycloakMaxLifeSeconds: Long,
    @Value("\${app.http.client.keycloak.pending-acquire-timeout-seconds:60}")
    private val keycloakPendingAcquireTimeoutSeconds: Long,
    @Value("\${app.http.client.keycloak.connect-timeout-millis:10000}")
    private val keycloakConnectTimeoutMillis: Int,
    @Value("\${app.http.client.keycloak.response-timeout-seconds:10}")
    private val keycloakResponseTimeoutSeconds: Long,
) {
    private val provider = ConnectionProvider.builder("apiPool")
        .maxConnections(generalMaxConnections)
        .pendingAcquireMaxCount(generalPendingAcquireMaxCount)
        .maxIdleTime(Duration.ofSeconds(generalMaxIdleSeconds))
        .maxLifeTime(Duration.ofSeconds(generalMaxLifeSeconds))
        .pendingAcquireTimeout(Duration.ofSeconds(generalPendingAcquireTimeoutSeconds))
        .evictInBackground(Duration.ofMinutes(1))
        .build()

    private val client = HttpClient.create(provider).let {
        val configured = if (wiretapEnabled) {
            it.wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG, AdvancedByteBufFormat.SIMPLE)
        } else {
            it
        }
        configured
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, generalConnectTimeoutMillis)
            .responseTimeout(Duration.ofSeconds(generalResponseTimeoutSeconds))
            .keepAlive(true)
            .doOnConnected { conn -> conn.addHandlerLast(LogbookClientHandler(logbook)) }
    }


    @Bean("generalWebClient")
    fun loadBalancedWebClient(
        loadBalancerFactory: ReactiveLoadBalancer.Factory<ServiceInstance>,

        ): WebClient {
        return WebClient.builder()
            .filter(ReactorLoadBalancerExchangeFilterFunction(loadBalancerFactory, emptyList()))
            .clientConnector(ReactorClientHttpConnector(client))
            .filter(languageFilter())
            .codecs { configurer ->
                configurer.defaultCodecs()
                    .maxInMemorySize(5 * 1024 * 1024)
            }
            .build()
    }

    @Bean("keycloakWebClient")
    fun keycloakWebClient(logbook: Logbook): WebClient {
        val provider = ConnectionProvider.builder("keycloakPool")
            .maxConnections(keycloakMaxConnections)
            .pendingAcquireMaxCount(keycloakPendingAcquireMaxCount)
            .maxIdleTime(Duration.ofSeconds(keycloakMaxIdleSeconds))
            .maxLifeTime(Duration.ofSeconds(keycloakMaxLifeSeconds))
            .pendingAcquireTimeout(Duration.ofSeconds(keycloakPendingAcquireTimeoutSeconds))
            .evictInBackground(Duration.ofMinutes(1))
            .build()

        val client = HttpClient.create(provider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, keycloakConnectTimeoutMillis)
            .responseTimeout(Duration.ofSeconds(keycloakResponseTimeoutSeconds))
            .keepAlive(true)
            .doOnConnected { it.addHandlerLast(LogbookClientHandler(logbook)) }

        client.warmup().block()

        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(client))
            .baseUrl(url)
            .build()
    }

    private fun languageFilter() = ExchangeFilterFunction { request, next ->
        Mono.deferContextual { ctx ->
            val lang = ctx.getOrDefault("lang", getDefaultUserLanguage())
            val mutatedRequest = ClientRequest.from(request)
                .header("Accept-Language", lang)
                .build()
            next.exchange(mutatedRequest)
        }
    }


}