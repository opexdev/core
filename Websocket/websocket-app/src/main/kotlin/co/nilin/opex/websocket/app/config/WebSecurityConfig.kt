package co.nilin.opex.websocket.app.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder

@Configuration
class WebSecurityConfig : WebSecurityConfigurerAdapter() {

    @Value("\${app.auth.cert-url}")
    private lateinit var jwkUrl: String

    override fun configure(web: WebSecurity) {
        web.ignoring().antMatchers("/actuator/health")
    }

    override fun configure(http: HttpSecurity) {
        http.httpBasic().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .authorizeRequests()
            .antMatchers("/ws/**").permitAll()
            .anyRequest().denyAll()
            .and()
            .oauth2ResourceServer()
            .jwt()
    }

    @Bean
    @Throws(Exception::class)
    fun jwtDecoder(): JwtDecoder {
        return NimbusJwtDecoder.withJwkSetUri(jwkUrl).build()
    }

}