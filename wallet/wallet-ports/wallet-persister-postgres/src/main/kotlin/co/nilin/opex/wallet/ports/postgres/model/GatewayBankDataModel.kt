package co.nilin.opex.wallet.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("gateway_bank_data")
data class GatewayBankDataModel(
    @Id
    var id: Long? = null,
    var bankDataId: Long,
    var gatewayId: Long,
)

