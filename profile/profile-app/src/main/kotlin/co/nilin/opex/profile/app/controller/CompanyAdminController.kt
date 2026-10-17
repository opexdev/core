package co.nilin.opex.profile.app.controller

import co.nilin.opex.profile.app.dto.CompanyAdminResponse
import co.nilin.opex.profile.app.dto.LegalAccountAvailabilityRequest
import co.nilin.opex.profile.app.dto.RegisterLegalAccountRequest
import co.nilin.opex.profile.app.service.CompanyManagement
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/company")
class CompanyAdminController(val companyManagement: CompanyManagement) {

    @PostMapping("/legal-account/availability")
    suspend fun ensureAvailable(@RequestBody request: LegalAccountAvailabilityRequest) {
        companyManagement.ensureAvailable(request)
    }

    @PostMapping("/legal-account")
    suspend fun registerLegalAccount(
        @RequestBody request: RegisterLegalAccountRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): CompanyAdminResponse {
        return companyManagement.registerLegalAccount(request, securityContext.authentication.name)
    }

    @GetMapping("/{uuid}")
    suspend fun getCompany(@PathVariable("uuid") uuid: String): CompanyAdminResponse {
        return companyManagement.getCompanyForAdmin(uuid)
    }
}
