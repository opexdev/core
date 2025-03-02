package co.nilin.opex.matching.gateway.app.controller

import co.nilin.opex.matching.gateway.ports.postgres.dto.PairSetting
import co.nilin.opex.matching.gateway.ports.postgres.service.PairSettingService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/pair-setting")
class PairSettingController(private val pairSettingService: PairSettingService) {

    data class UpdatePairSettingRequestBody(
        val pair: String,
        val isAvailable: Boolean
    )

    @GetMapping("/{pair}")
    suspend fun getPairSetting(@PathVariable pair: String): PairSetting {
        return pairSettingService.load(pair)
    }

    @GetMapping
    suspend fun getPairSettings(): List<PairSetting> {
        return pairSettingService.loadAll()
    }

    @PutMapping
    suspend fun updatePairSetting(@RequestBody updatePairSettingRequestBody: UpdatePairSettingRequestBody): PairSetting {
        return pairSettingService.update(updatePairSettingRequestBody.pair, updatePairSettingRequestBody.isAvailable)
    }
}