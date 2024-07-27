package co.nilin.opex.wallet.ports.postgres.util

import co.nilin.opex.wallet.core.inout.CryptoCurrencyCommand
import co.nilin.opex.wallet.core.inout.CurrencyCommand
import co.nilin.opex.wallet.core.inout.DepositMethod
import co.nilin.opex.wallet.core.inout.WithdrawMethod
import co.nilin.opex.wallet.ports.postgres.model.CurrencyModel
import java.math.BigDecimal
import java.util.*
import co.nilin.opex.wallet.core.inout.Deposit
import co.nilin.opex.wallet.ports.postgres.model.DepositModel
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
            withdrawAllowed,
            depositAllowed,
            withdrawFee,
            externalUrl,
            isCryptoCurrency,
            withdrawMin
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
            withdrawFee,
            null,
            null,
            externalUrl,
            isCryptoCurrency,
            null,
            withdrawMin
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