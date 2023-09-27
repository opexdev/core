package co.nilin.opex.config.ports.redis.document

import com.redis.om.spring.annotations.Document
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.index.Indexed

@Document
data class WebConfigDocument(
    var logoUrl: String,
    var title: String,
    var description: String?,
    var defaultLanguage: String,
    var supportedLanguages: List<String>,
    var defaultTheme: String,
    var supportEmail: String,
    var baseCurrency: String,
    var dateType: String,
    @Id
    @Indexed
    val id: String = ID
) {

    companion object {
        const val ID = "SystemConfig"

        fun default() = WebConfigDocument(
            "",
            "Opex",
            "",
            "en",
            listOf("en", "fa"),
            "DARK",
            "support@opex.dev",
            "USDT",
            "Jalali"
        )
    }
}