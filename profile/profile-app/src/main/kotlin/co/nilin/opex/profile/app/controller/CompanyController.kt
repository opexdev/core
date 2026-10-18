package co.nilin.opex.profile.app.controller

import co.nilin.opex.profile.app.dto.CompanyResponse
import co.nilin.opex.profile.app.service.CompanyManagement
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/company")
class CompanyController(val companyManagement: CompanyManagement) {

    @GetMapping
    suspend fun getCompany(@CurrentSecurityContext securityContext: SecurityContext): CompanyResponse {
        return companyManagement.getCompany(securityContext.authentication.name)
    }
}
