package co.nilin.opex.wallet.core.service.sample

import co.nilin.opex.wallet.core.inout.TransferCommand
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.model.Wallet
import co.nilin.opex.wallet.core.model.WalletOwner
import co.nilin.opex.wallet.ports.postgres.model.WalletLimitsModel
import java.math.BigDecimal

object VALID {
    const val USER_LEVEL_REGISTERED = "registered"

    const val ACTION_WITHDRAW = "withdraw"

    const val ACTION_DEPOSIT = "deposit"

    const val WALLET_TYPE_MAIN = "main"

    val CURRENCY = Currency("ETH", "Ethereum", BigDecimal.valueOf(0.0001))

    val SOURCE_WALLET_OWNER = WalletOwner(
        1L,
        "fdf453d7-0633-4ec7-852d-a18148c99a82",
        "wallet",
        "1",
        isTradeAllowed = true,
        isWithdrawAllowed = true,
        isDepositAllowed = true
    )

    val SOURCE_WALLET = Wallet(
        1L,
        SOURCE_WALLET_OWNER,
        Amount(CURRENCY, BigDecimal.valueOf(1.5)),
        CURRENCY,
        "main"
    )

    val DEST_WALLET_OWNER = SOURCE_WALLET_OWNER.copy(2, "e1950578-ef22-44e4-89f5-0b78feb03e2a")

    val DEST_WALLET = SOURCE_WALLET.copy(2, DEST_WALLET_OWNER)

    val WALLET_LIMITS_MODEL_WITHDRAW = WalletLimitsModel(
        1,
        USER_LEVEL_REGISTERED,
        SOURCE_WALLET_OWNER.id,
        ACTION_WITHDRAW,
        CURRENCY.symbol,
        WALLET_TYPE_MAIN,
        SOURCE_WALLET.id,
        BigDecimal.valueOf(100),
        10,
        BigDecimal.valueOf(3000),
        300
    )

    val WALLET_LIMITS_MODEL_DEPOSIT = WALLET_LIMITS_MODEL_WITHDRAW.copy(action = ACTION_DEPOSIT)
}