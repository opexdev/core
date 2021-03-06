package co.nilin.opex.websocket.ports.postgres.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@Configuration
@EnableR2dbcRepositories(basePackages = ["co.nilin.opex"])
class PostgresConfig
