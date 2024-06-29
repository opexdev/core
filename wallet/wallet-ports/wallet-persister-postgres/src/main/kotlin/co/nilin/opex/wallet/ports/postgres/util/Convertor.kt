package co.nilin.opex.wallet.ports.postgres.util

import co.nilin.opex.wallet.core.inout.Deposit
import co.nilin.opex.wallet.ports.postgres.model.DepositModel
import java.time.ZoneId
import java.util.*


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