package co.nilin.opex.wallet.ports.postgres.util

import co.nilin.opex.wallet.core.inout.Deposit
import co.nilin.opex.wallet.ports.postgres.model.DepositModel



    fun Deposit.toModel():DepositModel{
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
                createDate)
    }



fun DepositModel.toDto():Deposit{
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
            createDate)
}