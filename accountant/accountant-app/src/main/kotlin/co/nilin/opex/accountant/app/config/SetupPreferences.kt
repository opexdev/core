package co.nilin.opex.accountant.app.config

import co.nilin.opex.accountant.ports.postgres.dao.PairConfigRepository
import co.nilin.opex.accountant.ports.postgres.dao.PairFeeConfigRepository
import co.nilin.opex.accountant.ports.postgres.model.PairFeeConfigModel
import co.nilin.opex.utility.preferences.ProjectPreferences
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component

@Component
@DependsOn("postgresConfig")
class SetupPreferences(
    private val pairConfigRepository: PairConfigRepository,
    private val pairFeeConfigRepository: PairFeeConfigRepository
) {
    @Autowired
    private lateinit var preferences: ProjectPreferences

    @Autowired
    fun init() {
        runBlocking {
            preferences.markets.map {
                val pair = it.pair ?: "${it.leftSide}_${it.rightSide}"
                val leftSideCurrency = preferences.currencies.first { c -> it.leftSide == c.symbol }
                val rightSideCurrency = preferences.currencies.first { c -> it.rightSide == c.symbol }
                val leftSideFraction = (it.leftSideFraction ?: leftSideCurrency.precision).toDouble()
                val rightSideFraction = (it.rightSideFraction ?: rightSideCurrency.precision).toDouble()
                pairConfigRepository.insert(
                    pair,
                    it.leftSide,
                    it.rightSide,
                    leftSideFraction,
                    rightSideFraction,
                    0.0
                ).awaitSingleOrNull()
                it.feeConfigs.forEach { f ->
                    runCatching {
                        pairFeeConfigRepository.save(
                            PairFeeConfigModel(
                                null,
                                pair,
                                f.direction,
                                f.userLevel,
                                f.makerFee.toDouble(),
                                f.takerFee.toDouble()
                            )
                        ).awaitSingleOrNull()
                    }
                }
            }
        }
    }
}
