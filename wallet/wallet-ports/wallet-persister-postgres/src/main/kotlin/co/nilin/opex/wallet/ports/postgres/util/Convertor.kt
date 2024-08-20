package co.nilin.opex.wallet.ports.postgres.util

import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.ports.postgres.model.CurrencyModel
import java.util.*
import co.nilin.opex.wallet.ports.postgres.model.DepositModel
import co.nilin.opex.wallet.ports.postgres.model.OffChainGatewayModel
import java.math.BigDecimal
import java.time.ZoneId


fun CurrencyCommand.toModel(): CurrencyModel {
    return CurrencyModel(
            symbol,
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
            isCryptoCurrency
    )
}

fun CurrencyModel.toCommand(): CurrencyCommand {
    return CurrencyCommand(
            symbol,
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
            isCryptoCurrency,
            null
    )
}


fun Deposit.toModel(): DepositModel {
    return DepositModel(null,
            ownerUuid,
            depositUuid,
            currency,
            amount,
            acceptedFee,
            appliedFee,
            sourceSymbol,
            network,
            sourceAddress,
            note,
            transactionRef,
            status,
            depositType,
            createDate?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime())
}


fun DepositModel.toDto(): Deposit {
    return Deposit(
            ownerUuid,
            depositUuid,
            currency,
            amount,
            acceptedFee,
            appliedFee,
            sourceSymbol,
            network,
            sourceAddress,
            note,
            transactionRef,
            status,
            depositType,
            Date.from(createDate?.atZone(ZoneId.systemDefault())?.toInstant())
    )
}


fun OffChainGatewayModel.toDto(): CurrencyGatewayCommand {
    var offChainGatewayModel = OffChainGatewayCommand(TransferMethod.valueOf(transferMethod))
    return offChainGatewayModel.apply {
        type = GatewayType.OffChain
        currencySymbol = currencySymbol
        gatewayUuid = gatewayUuid
        isActive = isActive
        withdrawFee = withdrawFee
        withdrawAllowed = withdrawAllowed
        depositAllowed = depositAllowed
        depositMin = depositMin
        depositMax = depositMax
        withdrawMin = withdrawMin
        withdrawMax = withdrawMax

    }
}

fun OffChainGatewayCommand.toModel(): OffChainGatewayModel {
    return OffChainGatewayModel(null, gatewayUuid!!,
   currencySymbol!!,
   withdrawAllowed,
   depositAllowed,
   withdrawFee,
   withdrawMin,
   withdrawMax,
   depositMin,
   depositMax,
   transferMethod.name,
   isActive)
}