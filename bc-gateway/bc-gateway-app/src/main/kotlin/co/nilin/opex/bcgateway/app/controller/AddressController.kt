package co.nilin.opex.bcgateway.app.controller

import co.nilin.opex.bcgateway.core.api.AssignAddressService
import co.nilin.opex.bcgateway.core.model.AssignedAddress
import co.nilin.opex.bcgateway.core.model.Currency
import co.nilin.opex.bcgateway.core.model.ReservedAddress
import co.nilin.opex.bcgateway.core.spi.AddressTypeHandler
import co.nilin.opex.bcgateway.core.spi.ReservedAddressHandler
import co.nilin.opex.common.OpexError
import co.nilin.opex.utility.error.data.OpexException
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.io.File
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/v1/address")
class AddressController(
        private val assignAddressService: AssignAddressService,
        private val reservedAddressHandler: ReservedAddressHandler,
        private val addressTypeHandler: AddressTypeHandler
) {
    data class AssignAddressRequest(val uuid: String, val currency: String, val chain: String)
    data class AssignAddressResponse(val addresses: List<AssignedAddress>)

    @PostMapping("/assign")
    suspend fun assignAddress(@RequestBody assignAddressRequest: AssignAddressRequest,
                              @CurrentSecurityContext securityContext: SecurityContext?): AssignAddressResponse {
        if (!(securityContext == null || securityContext.authentication.name == assignAddressRequest.uuid))
            throw OpexError.Forbidden.exception()
        val assignedAddress = assignAddressService.assignAddress(
                assignAddressRequest.uuid,
                Currency(assignAddressRequest.currency, assignAddressRequest.currency),
                assignAddressRequest.chain
        )
        return AssignAddressResponse(assignedAddress)
    }

    /**
     * (address, regex, address_type)
     */
    @PutMapping
    suspend fun putAddresses(@RequestPart("file") file: Mono<FilePart>) {
        val f = File("reserved.csv")
        file.awaitSingle().transferTo(f).awaitSingleOrNull() ?: OpexError.BadRequest.exception("Invalid File")
        val csv = f.readLines(StandardCharsets.UTF_8)
        val addressTypes = addressTypeHandler.fetchAll().associateBy { it.type }
        val items = csv.map {
            val columns = it.split(",")
            if (columns.size != 3) throw OpexError.BadRequest.exception("Invalid CSV File")
            val at = addressTypes[columns[2]] ?: throw OpexError.BadRequest.exception("Invalid Address Type")
            ReservedAddress(columns[0], columns[1], at)
        }
        // Do nothing in case of duplication (Or any constraint issue)
        runCatching { reservedAddressHandler.addReservedAddress(items) }
    }
}
