package co.nilin.opex.bcgateway.app.controller

import co.nilin.opex.bcgateway.core.api.AssignAddressService
import co.nilin.opex.bcgateway.core.model.AssignedAddress
import co.nilin.opex.bcgateway.core.model.Currency
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AddressController(val assignAddressService: AssignAddressService) {

    data class AssignAddressRequest(val uuid: String, val currency: String)
    data class AssignAddressResponse(val addresses: List<AssignedAddress>)

    @PostMapping("/address/assign")
    suspend fun assignAddress(@RequestBody assignAddressRequest: AssignAddressRequest): AssignAddressResponse {
        val assignedAddress = assignAddressService
            .assignAddress(
                assignAddressRequest.uuid,
                Currency(assignAddressRequest.currency, assignAddressRequest.currency)
            )
        return AssignAddressResponse(assignedAddress)
    }

}