package co.nilin.opex.wallet.ports.postgres.util

import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.model.TotalAssetsSnapshot
import co.nilin.opex.wallet.ports.postgres.model.*
import java.time.ZoneId
import java.util.*


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
        externalUrl,
        order,
        maxOrder
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
        false,
        false,
        externalUrl,
        null,
        null,
        order,
        maxOrder
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
        description,
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
        description,
        displayOrder
    )
}

fun TerminalCommand.toModel(): TerminalModel {
    return TerminalModel(
        null,
        uuid,
        owner,
        identifier, active, type, metaData, description, displayOrder
    )
}

fun TerminalModel.toDto(): TerminalCommand {
    return TerminalCommand(
        uuid!!,
        owner,
        identifier, active, type, metaData, description, displayOrder
    )
}

fun CurrencyModel.toCurrencyData(): CurrencyData {
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
        order,
        maxOrder,
    )
}

fun TotalAssetsSnapshotModel.toTotalAssetsSnapshot(): TotalAssetsSnapshot {
    return TotalAssetsSnapshot(
        uuid,
        totalUSDT,
        totalIRT,
        snapshotDate
    )
}