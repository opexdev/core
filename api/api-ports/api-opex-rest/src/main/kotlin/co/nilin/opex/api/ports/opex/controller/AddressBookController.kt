package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.AddAddressBookItemRequest
import co.nilin.opex.api.core.inout.AddressBookResponse
import co.nilin.opex.api.core.spi.ProfileProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/opex/v1/address-book")
class AddressBookController(
    val profileProxy: ProfileProxy,
) {

    @PostMapping
    suspend fun addAddressBook(
        @RequestBody request: AddAddressBookItemRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): AddressBookResponse {
        return profileProxy.addAddressBook(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @GetMapping
    suspend fun getAddressBook(@CurrentSecurityContext securityContext: SecurityContext): List<AddressBookResponse> {
        return profileProxy.getAllAddressBooks(securityContext.jwtAuthentication().tokenValue())
    }

    @DeleteMapping("/{id}")
    suspend fun deleteAddressBook(
        @PathVariable("id") id: Long,
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        profileProxy.deleteAddressBook(securityContext.jwtAuthentication().tokenValue(), id)
    }

    @PutMapping("/{id}")
    suspend fun updateAddressBook(
        @PathVariable("id") id: Long,
        @RequestBody request: AddAddressBookItemRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): AddressBookResponse {
        return profileProxy.updateAddressBook(securityContext.jwtAuthentication().tokenValue(), id, request)
    }

}