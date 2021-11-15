package co.nilin.opex.matching.gateway.app.config

import co.nilin.opex.matching.gateway.app.inout.PairConfig
import co.nilin.opex.matching.gateway.app.inout.PairFeeConfig
import co.nilin.opex.matching.gateway.app.spi.AccountantApiProxy
import co.nilin.opex.matching.gateway.app.spi.PairConfigLoader
import co.nilin.opex.matching.core.model.OrderDirection
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal


@Configuration
class AppConfig {
    @Bean
    @ConditionalOnMissingBean(AccountantApiProxy::class)
    fun accountantApiProxy(): AccountantApiProxy {
        return object : AccountantApiProxy {
            override suspend fun canCreateOrder(uuid: String, symbol: String, value: BigDecimal): Boolean {
                return true
            }

            override suspend fun fetchPairFeeConfig(
                pair: String,
                direction: OrderDirection,
                userLevel: String
            ): PairFeeConfig {
                return PairFeeConfig(
                    PairConfig(
                        pair, pair.split("_")[0], pair.split("_")[1], 1.0, 1.0
                    ), direction.name, userLevel, 0.01, 0.01
                )
            }
        }
    }

    @Bean
    @ConditionalOnMissingBean(PairConfigLoader::class)
    fun pairConfigLoader(accountantApiProxy: AccountantApiProxy): PairConfigLoader {
        return object : PairConfigLoader {
            override suspend fun load(pair: String, direction: OrderDirection, userLevel: String): PairFeeConfig {
                return accountantApiProxy.fetchPairFeeConfig(pair, direction, userLevel)
            }
        }
    }
}