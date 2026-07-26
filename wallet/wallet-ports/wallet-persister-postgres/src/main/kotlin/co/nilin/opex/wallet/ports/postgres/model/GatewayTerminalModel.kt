package co.nilin.opex.wallet.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("gateway_terminal")
data class GatewayTerminalModel(
    @Id
    var id: Long? = null,
    var terminalId: Long,
    var gatewayId: Long,
)

