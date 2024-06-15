package co.nilin.opex.wallet.app.dto

data class CurrencyDocumentDtoResponse(var currencyUuid: String,
                          var documents: HashMap<DocumentType,String>)


enum class DocumentType{
    Image,
    HTML,
    Text,
    Video
}