package co.nilin.opex.wallet.ports.postgres.dto

import co.nilin.opex.wallet.core.model.WalletOwner
import co.nilin.opex.wallet.ports.postgres.model.WalletOwnerModel

fun WalletOwnerModel.toPlainObject() = WalletOwner(
    id,
    uuid,
    title,
    level,
    isTradeAllowed,
    isWithdrawAllowed,
    isDepositAllowed
)

fun WalletOwner.toModel() = WalletOwnerModel(
    id,
    uuid,
    title,
    level,
    isTradeAllowed,
    isWithdrawAllowed,
    isDepositAllowed
)