package co.nilin.opex.accountant.app.config

import co.nilin.opex.accountant.ports.postgres.dao.PairConfigRepository
import co.nilin.opex.accountant.ports.postgres.dao.PairFeeConfigRepository
import co.nilin.opex.accountant.ports.postgres.model.PairFeeConfigModel
import co.nilin.opex.utility.preferences.ProjectPreferences
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component
import java.io.File

@Component
@DependsOn("postgresConfig")
class SetupPreferences(
    @Value("preferences.yml") file: File,
    pairConfigRepository: PairConfigRepository,
    pairFeeConfigRepository: PairFeeConfigRepository
) {
    private val mapper = ObjectMapper(YAMLFactory())

    init {
        val p: ProjectPreferences = mapper.readValue(file, ProjectPreferences::class.java)
        runBlocking {
            p.markets.map {
                launch {
                    val pair = it.pair ?: "${it.leftSide}_${it.rightSide}"
                    val leftSideCurrency = p.currencies.first { c -> it.leftSide == c.symbol }
                    val rightSideCurrency = p.currencies.first { c -> it.rightSide == c.symbol }
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
                        launch {
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
}
