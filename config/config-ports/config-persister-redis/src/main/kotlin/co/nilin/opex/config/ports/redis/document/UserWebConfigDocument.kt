package co.nilin.opex.config.ports.redis.document

import com.redis.om.spring.annotations.Document
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.index.Indexed

@Document
data class UserWebConfigDocument(
    @Id
    @Indexed
    val userId: String,
    var theme: String,
    var language: String,
    var favoritePairs: HashSet<String> = hashSetOf()
) {

    companion object {
        fun default(uuid: String) = UserWebConfigDocument(uuid, "DARK", "en")
    }

}