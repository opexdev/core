package co.nilin.opex.referral.ports.postgres.repository

import co.nilin.opex.referral.ports.postgres.dao.Config
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface ConfigRepository : ReactiveCrudRepository<Config, String>