package co.nilin.opex.api.ports.binance.config

import io.netty.channel.ChannelOption
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

@Configuration
class WebClientConfig {

    @Bean
    @LoadBalanced
    fun webClientBuilder(): WebClient.Builder {
        return WebClient.builder()
    }

    @Bean
    fun webClient(webclientBuilder: WebClient.Builder): WebClient {
        val cp = ConnectionProvider.builder("apiBinanceWebclientConnectionPool")
            .maxConnectionPools(500)
            .maxIdleTime(Duration.ofSeconds(20))
            .maxLifeTime(Duration.ofSeconds(60))
            .pendingAcquireTimeout(Duration.ofSeconds(60))
            .evictInBackground(Duration.ofSeconds(120))
            .build()
        val client = HttpClient.create(cp)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 20000)
            .responseTimeout(Duration.ofSeconds(20))
            .keepAlive(true)
            .compress(true)
        return webclientBuilder.clientConnector(ReactorClientHttpConnector(client)).build()
    }
}
