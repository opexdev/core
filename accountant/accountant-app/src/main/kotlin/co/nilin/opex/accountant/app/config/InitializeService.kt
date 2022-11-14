package co.nilin.opex.accountant.app.config

import co.nilin.opex.accountant.ports.postgres.dao.PairConfigRepository
import co.nilin.opex.accountant.ports.postgres.dao.PairFeeConfigRepository
import co.nilin.opex.accountant.ports.postgres.dao.UserLevelRepository
import co.nilin.opex.accountant.ports.postgres.model.PairFeeConfigModel
import co.nilin.opex.utility.preferences.Preferences
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
@DependsOn("postgresConfig")
class InitializeService(
    private val pairConfigRepository: PairConfigRepository,
    private val pairFeeConfigRepository: PairFeeConfigRepository,
    private val userLevelRepository: UserLevelRepository,
) {

    @Autowired
    private lateinit var preferences: Preferences

    @PostConstruct
    fun init() = runBlocking {
        preferences.userLevels.forEach {
            userLevelRepository.insert(it).awaitSingleOrNull()
        }

        preferences.markets.map {
            val pair = it.pair ?: "${it.leftSide}_${it.rightSide}"
            val leftSideCurrency = preferences.currencies.first { c -> it.leftSide == c.symbol }
            val rightSideCurrency = preferences.currencies.first { c -> it.rightSide == c.symbol }
            val leftSideFraction = (it.leftSideFraction ?: leftSideCurrency.precision)
            val rightSideFraction = (it.rightSideFraction ?: rightSideCurrency.precision)
            pairConfigRepository.insert(
                pair,
                it.leftSide,
                it.rightSide,
                leftSideFraction,
                rightSideFraction
            ).awaitSingleOrNull()
            it.feeConfigs.forEach { f ->
                runCatching {
                    pairFeeConfigRepository.save(
                        PairFeeConfigModel(
                            null,
                            pair,
                            f.direction,
                            f.userLevel,
                            f.makerFee,
                            f.takerFee
                        )
                    ).awaitSingleOrNull()
                }
            }
        }
    }
}
