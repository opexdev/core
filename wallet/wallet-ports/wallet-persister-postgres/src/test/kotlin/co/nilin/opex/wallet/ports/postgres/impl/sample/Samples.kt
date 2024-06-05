package co.nilin.opex.wallet.ports.postgres.impl.sample

import co.nilin.opex.wallet.core.inout.CurrencyCommand
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.Wallet
import co.nilin.opex.wallet.core.model.WalletOwner
import co.nilin.opex.wallet.ports.postgres.model.WalletLimitsModel
import java.math.BigDecimal
import java.util.Currency

object VALID {
    const val USER_LEVEL_REGISTERED = "registered"

    const val ACTION_WITHDRAW = "withdraw"

    const val ACTION_DEPOSIT = "deposit"

    const val WALLET_TYPE_MAIN = "main"

    val CURRENCY = CurrencyCommand("ETH", null,"Ethereum", BigDecimal.valueOf(0.0001),id=1)

    val WALLET_OWNER = WalletOwner(
        1L,
        "fdf453d7-0633-4ec7-852d-a18148c99a82",
        "wallet",
        USER_LEVEL_REGISTERED,
        isTradeAllowed = true,
        isWithdrawAllowed = true,
        isDepositAllowed = true
    )

    val WALLET = Wallet(
        1L,
        WALLET_OWNER,
        Amount(CURRENCY, BigDecimal.valueOf(1.5)),
        CURRENCY,
        WALLET_TYPE_MAIN,
        0
    )

    val WALLET_LIMITS_MODEL_WITHDRAW = WalletLimitsModel(
        1,
        USER_LEVEL_REGISTERED,
        WALLET_OWNER.id,
        ACTION_WITHDRAW,
        CURRENCY.id!!,
        WALLET_TYPE_MAIN,
        WALLET.id,
        BigDecimal.valueOf(10),
        1,
        BigDecimal.valueOf(300),
        30
    )

    val WALLET_LIMITS_MODEL_DEPOSIT = WALLET_LIMITS_MODEL_WITHDRAW.copy(action = ACTION_DEPOSIT)

    val USER_LIMITS_MODEL_WITHDRAW = WalletLimitsModel(
        1,
        USER_LEVEL_REGISTERED,
        WALLET_OWNER.id,
        ACTION_WITHDRAW,
        CURRENCY.id!!,
        WALLET_TYPE_MAIN,
        WALLET.id,
        BigDecimal.valueOf(10),
        1,
        BigDecimal.valueOf(300),
        30
    )

    val USER_LIMITS_MODEL_DEPOSIT = USER_LIMITS_MODEL_WITHDRAW.copy(action = ACTION_DEPOSIT)
}
