package co.nilin.opex.profile.core.utils

import co.nilin.opex.common.OpexError

private fun handleError(code: String?, mapping: Map<String, OpexError>, default: OpexError) {
    if (code.isNullOrEmpty()) return
    throw mapping[code]?.exception() ?: default.exception()
}

fun handleShahkarError(code: String?) {
    handleError(
        code,
        mapOf(
            "invalid.request_body" to OpexError.InvalidRequestParam,
            "nationalCode.not_valid" to OpexError.InvalidNationalCode,
            "mobileNumber.not_valid" to OpexError.InvalidMobileNumber
        ),
        OpexError.ShahkarInquiryError
    )
}

fun handleComparativeError(code: String?) {
    handleError(
        code,
        mapOf(
            "birthDate.not_valid" to OpexError.InvalidBirthDate,
            "invalid.request_body" to OpexError.InvalidRequestParam,
            "query_parameters.not_provided" to OpexError.InvalidRequestParam,
            "identity_info.not_found" to OpexError.IdentityInfoNotFound,
            "required_is.nationalCode" to OpexError.InvalidNationalCode,
            "nationalCode.not_valid" to OpexError.InvalidNationalCode,
            "birthDate.is_required" to OpexError.InvalidBirthDate
        ),
        OpexError.ComparativeInquiryError
    )

}