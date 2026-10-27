package co.nilin.opex.bcgateway.app.controller

import co.nilin.opex.bcgateway.core.api.AddressAdminService
import co.nilin.opex.bcgateway.core.model.AddressAssignmentFilter
import co.nilin.opex.bcgateway.core.model.AddressAssignmentPage
import co.nilin.opex.bcgateway.core.model.AddressAssignmentRecord
import co.nilin.opex.bcgateway.core.model.AdminAssignAddressRequest
import co.nilin.opex.bcgateway.core.model.ReservedAddressFilter
import co.nilin.opex.bcgateway.core.model.ReservedAddressPage
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/address")
class AddressAdminController(private val addressAdminService: AddressAdminService) {


    @PostMapping("/assignments/search")
    suspend fun search(@RequestBody filter: AddressAssignmentFilter): AddressAssignmentPage {
        return addressAdminService.search(filter)
    }

    @GetMapping("/assignments/{id}")
    suspend fun getAssignment(@PathVariable("id") id: Long): AddressAssignmentRecord {
        return addressAdminService.findById(id)
    }

    @PostMapping("/reserved/search")
    suspend fun listAvailable(@RequestBody filter: ReservedAddressFilter): ReservedAddressPage {
        return addressAdminService.listAvailableAddresses(filter)
    }

    @PostMapping("/assign")
    suspend fun assign(@RequestBody request: AdminAssignAddressRequest): AddressAssignmentRecord {
        return addressAdminService.assign(request)
    }

    @PostMapping("/assignments/{id}/revoke")
    suspend fun revoke(@PathVariable("id") id: Long): AddressAssignmentRecord {
        return addressAdminService.revoke(id)
    }
}
