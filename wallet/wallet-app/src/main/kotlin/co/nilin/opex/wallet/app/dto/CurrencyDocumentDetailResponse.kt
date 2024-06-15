package co.nilin.opex.wallet.app.dto

data class CurrencyDocumentDetailResponse(
        var documentUuid: String,
        var currencyUuid: String,
        var type: DocumentType,
        var content: String,
        var title: String
)