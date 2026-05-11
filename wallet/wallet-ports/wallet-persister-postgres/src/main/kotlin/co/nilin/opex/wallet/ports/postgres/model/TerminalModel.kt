package co.nilin.opex.wallet.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("terminal")
data class TerminalModel(
    @Id
    var id: Long?,
    var uuid: String? = UUID.randomUUID().toString(),
    var identifier: String,
    var active: Boolean? = true,
    var metaData: String,
    var displayOrder: Int? = null,
)