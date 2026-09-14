package co.nilin.opex.price.ports.wallet.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WalletWebClientConfig {

    @LoadBalanced
    @Bean("walletWebClientBuilder")
    fun walletWebClientBuilder(@Value("\${app.wallet.url}") walletUrl: String): WebClient.Builder {
        return WebClient.builder().baseUrl(walletUrl)
    }

    @Bean
    fun walletWebClient(@Qualifier("walletWebClientBuilder") builder: WebClient.Builder): WebClient {
        return builder.build()
    }
}
