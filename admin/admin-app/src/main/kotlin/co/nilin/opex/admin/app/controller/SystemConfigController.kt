package co.nilin.opex.admin.app.controller

import co.nilin.opex.admin.app.data.AddCurrencyRequest
import co.nilin.opex.admin.app.data.EditCurrencyRequest
import co.nilin.opex.admin.app.service.SystemConfigService
import co.nilin.opex.admin.app.service.WhiteListManagement
import co.nilin.opex.admin.core.data.WhitelistAdaptor
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import org.springframework.web.bind.annotation.*
import javax.ws.rs.core.Response

@RestController
@RequestMapping("/system/v1")
class SystemConfigController(private val service: SystemConfigService,
                             private val whiteListManagement: WhiteListManagement) {

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

    @PostMapping("/whitelist")
    suspend fun updateWhitelist(@RequestBody users: WhitelistAdaptor): WhitelistAdaptor? {
        return whiteListManagement.addToWhiteList(users)

    }

    @GetMapping("/whitelist")
    suspend fun getWhitelist(): WhitelistAdaptor? {
        return whiteListManagement.getWhiteList()
    }

    @DeleteMapping("/whitelist")
    suspend fun deleteWhitelist(@RequestBody users: WhitelistAdaptor): WhitelistAdaptor? {
        return whiteListManagement.deleteFromWhiteList(users)

    }
}