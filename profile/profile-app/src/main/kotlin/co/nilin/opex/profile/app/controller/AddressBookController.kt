package co.nilin.opex.profile.app.controller

import co.nilin.opex.profile.app.dto.AddAddressBookItemRequest
import co.nilin.opex.profile.app.dto.AddressBookResponse
import co.nilin.opex.profile.app.service.AddressBookManagement
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/address-book")
class AddressBookController(
    val addressBookManagement: AddressBookManagement,
) {

    @PostMapping
    suspend fun addAddressBook(
        @RequestBody request: AddAddressBookItemRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): AddressBookResponse {
        return addressBookManagement.addAddressBook(securityContext.authentication.name, request)
    }

    @GetMapping
    suspend fun getAddressBook(@CurrentSecurityContext securityContext: SecurityContext): List<AddressBookResponse> {
        return addressBookManagement.getAllAddressBooks(securityContext.authentication.name)
    }

    @DeleteMapping("/{id}")
    suspend fun deleteAddressBook(
        @PathVariable("id") id: Long,
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        addressBookManagement.deleteAddressBook(securityContext.authentication.name, id)
    }

    @PutMapping("/{id}")
    suspend fun updateAddressBook(
        @PathVariable("id") id: Long,
        @RequestBody request: AddAddressBookItemRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): AddressBookResponse {
        return addressBookManagement.updateAddressBook(securityContext.authentication.name, id, request)
    }

}