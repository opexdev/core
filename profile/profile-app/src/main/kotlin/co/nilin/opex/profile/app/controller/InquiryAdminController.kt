package co.nilin.opex.profile.app.controller

import co.nilin.opex.profile.app.dto.CardInfoInquiryRequest
import co.nilin.opex.profile.app.dto.CardOwnershipInquiryRequest
import co.nilin.opex.profile.app.dto.IbanInfoInquiryRequest
import co.nilin.opex.profile.app.dto.IbanOwnershipInquiryRequest
import co.nilin.opex.profile.app.dto.IdentitySimilarityInquiryRequest
import co.nilin.opex.profile.app.dto.ShahkarInquiryRequest
import co.nilin.opex.profile.app.service.InquiryManagement
import co.nilin.opex.profile.core.data.inquiry.ComparativeResponse
import co.nilin.opex.profile.core.data.inquiry.IbanInfoResponse
import co.nilin.opex.profile.core.data.inquiry.ShahkarResponse
import co.nilin.opex.profile.core.data.inquiry.VerifyOwnershipResponse
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/inquiry")
class InquiryAdminController(
    val inquiryManagement: InquiryManagement
) {

    @PostMapping("/shahkar")
    suspend fun shahkar(
        @RequestBody request: ShahkarInquiryRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): ShahkarResponse {
        return inquiryManagement.shahkar(request, securityContext.authentication.name)
    }

    @PostMapping("/identity-similarity")
    suspend fun identitySimilarity(
        @RequestBody request: IdentitySimilarityInquiryRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): ComparativeResponse {
        return inquiryManagement.identitySimilarity(request, securityContext.authentication.name)
    }

    @PostMapping("/card-ownership")
    suspend fun cardOwnership(
        @RequestBody request: CardOwnershipInquiryRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): VerifyOwnershipResponse {
        return inquiryManagement.cardOwnership(request, securityContext.authentication.name)
    }

    @PostMapping("/iban-ownership")
    suspend fun ibanOwnership(
        @RequestBody request: IbanOwnershipInquiryRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): VerifyOwnershipResponse {
        return inquiryManagement.ibanOwnership(request, securityContext.authentication.name)
    }

    @PostMapping("/card-info")
    suspend fun cardInfo(
        @RequestBody request: CardInfoInquiryRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): IbanInfoResponse {
        return inquiryManagement.cardInfo(request, securityContext.authentication.name)
    }

    @PostMapping("/iban-info")
    suspend fun ibanInfo(
        @RequestBody request: IbanInfoInquiryRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): IbanInfoResponse {
        return inquiryManagement.ibanInfo(request, securityContext.authentication.name)
    }
}
