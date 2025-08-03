package co.nilin.opex.api.ports.binance.config

import co.nilin.opex.api.core.spi.APIKeyFilter
import co.nilin.opex.common.security.CustomJwtConverter
import jakarta.servlet.Filter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@EnableWebSecurity
@Configuration
class SecurityConfig(
    private val apiKeyFilter: APIKeyFilter,
    @Value("\${app.auth.cert-url}")
    private val jwkUrl: String
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http.csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/swagger-resources/**").permitAll()
                    .requestMatchers("/v2/api-docs").permitAll()
                    .requestMatchers("/v3/depth").permitAll()
                    .requestMatchers("/v3/trades").permitAll()
                    .requestMatchers("/v3/ticker/**").permitAll()
                    .requestMatchers("/v3/exchangeInfo").permitAll()
                    .requestMatchers("/v3/currencyInfo/**").permitAll()
                    .requestMatchers("/v3/klines").permitAll()
                    .requestMatchers("/socket").permitAll()
                    .requestMatchers("/v1/landing/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/v3/order").hasAuthority("PERM_order:write")
                    .requestMatchers(HttpMethod.DELETE, "/v3/order").hasAuthority("PERM_order:write")

                    // Opex endpoints
                    .requestMatchers("/opex/v1/deposit/**").hasAuthority("DEPOSIT_deposit:write")
                    .requestMatchers(HttpMethod.POST, "/opex/v1/order").hasAuthority("PERM_order:write")
                    .requestMatchers(HttpMethod.PUT, "/opex/v1/order").hasAuthority("PERM_order:write")
                    .requestMatchers(HttpMethod.POST, "/opex/v1/withdraw").hasAuthority("PERM_withdraw:write")
                    .requestMatchers(HttpMethod.PUT, "/opex/v1/withdraw").hasAuthority("PERM_withdraw:write")
                    .requestMatchers("/opex/v1/voucher").hasAuthority("PERM_voucher:submit")
                    .requestMatchers("/opex/v1/market/**").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(apiKeyFilter as Filter, UsernamePasswordAuthenticationFilter::class.java)
            .oauth2ResourceServer { it.jwt { jwt -> jwt.jwtAuthenticationConverter(CustomJwtConverter()) } }
            .build()
    }

    @Bean
    @Throws(Exception::class)
    fun jwtDecoder(): JwtDecoder? {
        return NimbusJwtDecoder.withJwkSetUri(jwkUrl).build()
    }
}
