package co.nilin.opex.wallet.core.service.sample

import java.math.BigDecimal

object VALID {
    private const val USER_LEVEL_REGISTERED = "registered"

    const val ACTION_WITHDRAW = "withdraw"

    const val ACTION_DEPOSIT = "deposit"

    private const val WALLET_TYPE_MAIN = "main"

    val CURRENCY = Currency("ETH", "Ethereum", BigDecimal.valueOf(0.0001))

    val SOURCE_WALLET_OWNER = WalletOwner(
        1L,
        "fdf453d7-0633-4ec7-852d-a18148c99a82",
        "wallet",
        USER_LEVEL_REGISTERED,
        isTradeAllowed = true,
        isWithdrawAllowed = true,
        isDepositAllowed = true
    )

    val SOURCE_WALLET = Wallet(
        1L,
        SOURCE_WALLET_OWNER,
        Amount(CURRENCY, BigDecimal.valueOf(1.5)),
        CURRENCY,
        WALLET_TYPE_MAIN
        , 0
    )

    val DEST_WALLET_OWNER = SOURCE_WALLET_OWNER.copy(2, "e1950578-ef22-44e4-89f5-0b78feb03e2a")

    val DEST_WALLET = SOURCE_WALLET.copy(2, DEST_WALLET_OWNER)

    val TRANSFER_COMMAND = TransferCommand(
        SOURCE_WALLET,
        DEST_WALLET,
        Amount(CURRENCY, BigDecimal.valueOf(0.5)),
        null,
        null,
        "NORMAL",
        emptyMap()
    )
}
