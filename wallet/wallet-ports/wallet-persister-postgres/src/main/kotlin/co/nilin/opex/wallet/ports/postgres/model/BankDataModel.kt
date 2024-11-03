package co.nilin.opex.wallet.ports.postgres.model

import co.nilin.opex.wallet.core.inout.TransferMethod
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.util.*

@Table("bank_data")
data class BankDataModel (
    @Id
    var id: Long?,
    var uuid: String? = UUID.randomUUID().toString(),
    var owner: String,
    var identifier: String,
    var active:Boolean?=true,
    var type:TransferMethod,
    var bankSwiftCode:String,
)