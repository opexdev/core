package co.nilin.opex.admin.app.controller

import co.nilin.opex.admin.app.data.AddCurrencyRequest
import co.nilin.opex.admin.app.data.EditCurrencyRequest
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

    @PutMapping("/currency/{name}")
    suspend fun editCurrency(@RequestBody body: EditCurrencyRequest, @PathVariable name: String) {
        if (!body.isValid())
            throw OpexException(OpexError.BadRequest)
        service.editCurrency(name, body)
    }

    @DeleteMapping("/currency/{name}")
    suspend fun deleteCurrency(@PathVariable name: String) {
        service.deleteCurrency(name)
    }

}