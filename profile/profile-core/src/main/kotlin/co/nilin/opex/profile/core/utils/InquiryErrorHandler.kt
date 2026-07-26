package co.nilin.opex.profile.core.utils

import co.nilin.opex.common.OpexError

private fun handleError(code: String?, mapping: Map<String, OpexError>, default: OpexError) {
    if (code.isNullOrEmpty()) return
    throw mapping[code]?.exception() ?: default.exception()
}

object ErrorMappings {

    val shahkar = mapOf(
        "invalid.request_body" to OpexError.InvalidRequestParam,
        "nationalCode.not_valid" to OpexError.InvalidNationalCode,
        "mobileNumber.not_valid" to OpexError.InvalidMobileNumber
    )

    val comparative = mapOf(
        "birthDate.not_valid" to OpexError.InvalidBirthDate,
        "invalid.request_body" to OpexError.InvalidRequestParam,
        "query_parameters.not_provided" to OpexError.InvalidRequestParam,
        "identity_info.not_found" to OpexError.IdentityInfoNotFound,
        "required_is.nationalCode" to OpexError.InvalidNationalCode,
        "nationalCode.not_valid" to OpexError.InvalidNationalCode,
        "birthDate.is_required" to OpexError.InvalidBirthDate
    )

    val cardOwnership = mapOf(
        "invalid.request_body" to OpexError.InvalidRequestBody,
        "forbidden" to OpexError.Forbidden,
        "card.not_valid" to OpexError.InvalidCard,
        "card.not_active" to OpexError.CardNotActive,
        "card.is_expired" to OpexError.CardIsExpired,
        "card.account_number_not_valid" to OpexError.CardAccountNumberNotValid,
        "card.registered_as_lost" to OpexError.CardRegisteredAsLost,
        "card.registered_as_stolen" to OpexError.CardRegisteredAsStolen,
        "card.source_bank_is_not_active" to OpexError.CardSourceBankNotActive,
        "card.black_listed" to OpexError.CardBlackListed,
        "identity_info.not_found" to OpexError.IdentityInfoNotFound
    )

    val ibanOwnership = mapOf(
        "invalid.request_body" to OpexError.InvalidRequestBody,
        "forbidden" to OpexError.Forbidden,
        "iban.is_required" to OpexError.InvalidRequestBody,
        "iban.not_valid" to OpexError.InvalidIban,
        "identity_info.not_found" to OpexError.IdentityInfoNotFound
    )

    val ibanInfo = mapOf(
        "invalid.request_body" to OpexError.InvalidRequestBody,
        "forbidden" to OpexError.Forbidden,
        "iban.is_required" to OpexError.IbanIsRequired,
        "iban.not_valid" to OpexError.InvalidIban,
        "iban.not_found" to OpexError.IbanNotFound,
        "iban.owner_not_found" to OpexError.IbanOwnerNotFound
    )

    val cardIbanInfo = mapOf(
        "invalid.request_body" to OpexError.InvalidRequestBody,
        "forbidden" to OpexError.Forbidden,
        "card.required" to OpexError.CardRequired,
        "card.is_required" to OpexError.CardRequired,
        "card.not_valid" to OpexError.InvalidCard,
        "card.type.not_supported" to OpexError.CardTypeNotSupported,
        "parameters.not_acceptable" to OpexError.InvalidRequestParam,
        "card.not_active" to OpexError.CardNotActive,
        "card.is_expired" to OpexError.CardIsExpired,
        "card.account_number_not_valid" to OpexError.CardAccountNumberNotValid,
        "card_number.not_valid" to OpexError.InvalidCard,
        "iban.not_found" to OpexError.IbanNotFound,
        "iban.not_valid" to OpexError.InvalidIban,
        "iban.owner.not_found" to OpexError.IbanOwnerNotFound,
        "card.black_listed" to OpexError.CardBlackListed,
        "card.registered_as_lost" to OpexError.CardRegisteredAsLost,
        "card.registered_as_stolen" to OpexError.CardRegisteredAsStolen,
        "card.source_bank_is_not_active" to OpexError.CardSourceBankNotActive,
        "iban.black_listed" to OpexError.IbanBlackListed
    )
}

fun handleShahkarError(code: String?) =
    handleError(code, ErrorMappings.shahkar, OpexError.ShahkarInquiryError)

fun handleComparativeError(code: String?) =
    handleError(code, ErrorMappings.comparative, OpexError.ComparativeInquiryError)

fun handleCardOwnershipError(code: String?) =
    handleError(code, ErrorMappings.cardOwnership, OpexError.CardOwnershipInquiryError)

fun handleIbanOwnershipError(code: String?) =
    handleError(code, ErrorMappings.ibanOwnership, OpexError.IbanOwnershipInquiryError)

fun handleIbanInfoError(code: String?) =
    handleError(code, ErrorMappings.ibanInfo, OpexError.IbanInfoInquiryError)

fun handleCardIbanInfoError(code: String?) =
    handleError(code, ErrorMappings.cardIbanInfo, OpexError.CardIbanInfoInquiryError)