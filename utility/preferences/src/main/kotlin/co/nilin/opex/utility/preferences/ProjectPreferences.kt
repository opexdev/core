package co.nilin.opex.utility.preferences

data class ProjectPreferences(
    var chains: List<Chain>, var currencies: List<Currency>, var wallets: List<Wallet>, var userLevels: List<UserLevel>
)