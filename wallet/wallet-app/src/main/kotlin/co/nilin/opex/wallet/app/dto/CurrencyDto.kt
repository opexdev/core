package co.nilin.opex.wallet.app.dto

import co.nilin.opex.wallet.core.inout.CurrencyCommand
import co.nilin.opex.wallet.core.inout.CurrencyGatewayCommand
import java.math.BigDecimal
import java.util.*

data class CurrencyDto(
    var symbol: String? = null,
    var uuid: String? = UUID.randomUUID().toString(),
    var name: String,
    var precision: BigDecimal,
    var title: String? = null,
    var alias: String? = null,
    var icon: String? = null,
    var isTransitive: Boolean? = false,
    var isActive: Boolean? = true,
    var sign: String? = null,
    var description: String? = null,
    var shortDescription: String? = null,
    var withdrawAllowed: Boolean? = false,
    var depositAllowed: Boolean? = false,
    var externalUrl: String? = null,
    var gateways: List<CurrencyGatewayCommand>? = null,
    var availableGatewayType: String? = null,
    var order: Int? = null,
    var systemBalance: BigDecimal? = null,
    var maxOrder: BigDecimal? = null,

) {

    // Separated them just to support having "id" field in currency table.
    //Now it is unnecessary
    fun toCommand(): CurrencyCommand {

        return CurrencyCommand(
            symbol!!,
            uuid,
            name,
            precision,
            title,
            alias,
            icon,
            isTransitive,
            isActive,
            sign,
            description,
            shortDescription,
            withdrawAllowed,
            depositAllowed,
            externalUrl,
            gateways,
            availableGatewayType,
            order,
            maxOrder
        )
    }
}


data class CurrenciesDto(var currencies: List<CurrencyDto>?)
