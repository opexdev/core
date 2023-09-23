package co.nilin.opex.config.ports.redis.document

import com.redis.om.spring.annotations.Document
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.index.Indexed

@Document
data class SystemConfig(
    val logoUrl: String,
    val title: String,
    val description: String?,
    val defaultLanguage: String,
    val supportedLanguages: List<String>,
    val defaultTheme: String,
    val supportEmail: String,
    val baseCurrency: String,
    val dateType: String,
    @Id
    @Indexed
    val id: String? = null,
)