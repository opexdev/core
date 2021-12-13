package co.nilin.opex.referral.app.controller

import co.nilin.opex.referral.core.spi.ReferralCodeHandler
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class CommissionController(private val referralCodeHandler: ReferralCodeHandler) {
    @GetMapping("/commissions/{code}")
    suspend fun getCommissionsByCode(@PathVariable code: String) {
        TODO("Not yet implemented")
    }

    @GetMapping("/{uuid}/commissions/{code}")
    suspend fun getCommissionsByUuidAndCode(@PathVariable code: String, @PathVariable uuid: String) {
        TODO("Not yet implemented")
    }

    @DeleteMapping("/commissions/{code}")
    suspend fun deleteCommissionsByCode(@PathVariable code: String) {
        TODO("Not yet implemented")
    }

    @DeleteMapping("/{uuid}/commissions/{code}")
    suspend fun deleteCommissionsByUuid(@PathVariable code: String) {
        TODO("Not yet implemented")
    }
}
