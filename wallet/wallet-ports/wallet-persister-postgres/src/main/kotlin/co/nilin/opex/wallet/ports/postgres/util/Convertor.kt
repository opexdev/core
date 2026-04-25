package co.nilin.opex.wallet.ports.postgres.util

import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.model.TotalAssetsSnapshot
import co.nilin.opex.wallet.ports.postgres.dto.CurrencyView
import co.nilin.opex.wallet.ports.postgres.dto.TerminalView
import co.nilin.opex.wallet.ports.postgres.model.*
import java.time.ZoneId
import java.util.*


fun CurrencyCommand.toView(): CurrencyView {
    return CurrencyView(
        symbol,
        uuid!!,
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
        externalUrl,
        displayOrder,
        maxOrder
    )
}

fun CurrencyView.toModel(): CurrencyModel {
    return CurrencyModel(
        symbol,
        uuid,
        precision,
        icon,
        isTransitive,
        isActive,
        sign,
        externalUrl,
        displayOrder,
        maxOrder
    )
}

fun CurrencyCommand.toModel(): CurrencyModel {
    return CurrencyModel(
        symbol,
        uuid,
        precision,
        icon,
        isTransitive,
        isActive,
        sign,
        externalUrl,
        displayOrder,
        maxOrder
    )
}

fun CurrencyView.toCommand(): CurrencyCommand {
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
        false,
        false,
        externalUrl,
        null,
        null,
        displayOrder,
        maxOrder
    )
}

fun CurrencyLocalizationModel.toCommand(): CurrencyLocalizationCommand {
    return CurrencyLocalizationCommand(
        id,
        name,
        title,
        alias,
        description,
        shortDescription,
        language
    )
}


fun Deposit.toModel(): DepositModel {
    return DepositModel(
        id,
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
        attachment,
        depositType,
        createDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
        transferMethod
    )
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
        transactionRef,
        note,
        status,
        depositType,
        attachment,
        Date.from(createDate.atZone(ZoneId.systemDefault())?.toInstant()),
        id,
        transferMethod
    )
}


fun OffChainGatewayModel.toDto(): CurrencyGatewayCommand {
    return OffChainGatewayCommand(
        TransferMethod.valueOf(transferMethod),
        currencySymbol,
        gatewayUuid,
        isDepositActive,
        isWithdrawActive,
        withdrawFee,
        withdrawAllowed,
        depositAllowed,
        depositMin,
        depositMax,
        withdrawMin,
        withdrawMax,
        depositDescription,
        withdrawDescription,
        displayOrder
    )

}

fun OffChainGatewayCommand.toModel(): OffChainGatewayModel {
    return OffChainGatewayModel(
        null, gatewayUuid!!,
        currencySymbol!!,
        withdrawAllowed,
        depositAllowed,
        withdrawFee,
        withdrawMin,
        withdrawMax,
        depositMin,
        depositMax,
        transferMethod.name,
        isDepositActive,
        isWithdrawActive,
        depositDescription,
        withdrawDescription,
        displayOrder
    )
}

fun TerminalCommand.toModel(): TerminalModel {
    return TerminalModel(
        null,
        uuid,
        owner,
        identifier, active, type, metaData, displayOrder
    )
}

fun TerminalModel.toDto(): TerminalCommand {
    return TerminalCommand(
        uuid!!,
        owner,
        identifier, active, type, metaData, null, displayOrder
    )
}

fun TerminalView.toModel(): TerminalModel {
    return TerminalModel(
        id,
        uuid,
        owner,
        identifier, active, type, metaData, displayOrder
    )
}

fun TerminalView.toCommand(): TerminalCommand {
    return TerminalCommand(
        uuid,
        owner,
        identifier, active, type, metaData, description, displayOrder
    )
}

fun TerminalLocalizationModel.toCommand(): TerminalLocalizationCommand {
    return TerminalLocalizationCommand(
        id,
        description,
        language
    )
}

fun CurrencyView.toCurrencyData(): CurrencyData {
    return CurrencyData(
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
        externalUrl,
        displayOrder,
        maxOrder,
    )
}

fun TotalAssetsSnapshotModel.toTotalAssetsSnapshot(): TotalAssetsSnapshot {
    return TotalAssetsSnapshot(
        uuid,
        totalAmount,
        quoteCurrency,
        snapshotDate
    )
}