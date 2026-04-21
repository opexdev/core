package co.nilin.opex.wallet.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("terminal_localization")
data class TerminalLocalizationModel(
    @Id
    var id: Long? = null,
    var terminalId: Long,
    var description: String,
    var language: String,
)