package co.nilin.opex.admin.app.controller

import co.nilin.opex.admin.app.data.AddCurrencyRequest
import co.nilin.opex.admin.app.service.SystemConfigService
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/system/v1")
class SystemConfigController(private val service: SystemConfigService) {

    @PostMapping("/currency")
    suspend fun addCurrency(@RequestBody body: AddCurrencyRequest) {
        if (!body.isValid())
            throw OpexException(OpexError.BadRequest)
        service.addCurrency(body)
    }

    @PutMapping("/currency")
    suspend fun editCurrency() {

    }

    @PostMapping("/pair")
    suspend fun addOrderBook() {

    }

    @PutMapping("/pair")
    suspend fun editPair() {

    }

}