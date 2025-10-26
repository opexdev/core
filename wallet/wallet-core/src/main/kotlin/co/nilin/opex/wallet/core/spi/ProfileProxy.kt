package co.nilin.opex.wallet.core.spi

interface ProfileProxy {

    suspend fun verifyBankAccountOwnership(
        token: String,
        cardNumber: String? = null,
        iban: String? = null,
    ): Boolean
}