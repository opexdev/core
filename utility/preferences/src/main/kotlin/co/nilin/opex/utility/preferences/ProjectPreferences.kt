package co.nilin.opex.utility.preferences

data class ProjectPreferences(
    var addressTypes: List<AddressType>,
    var chains: List<Chain>,
    var currencies: List<Currency>,
    var markets: List<Market>,
    var wallets: List<Wallet>,
    var userLevels: List<UserLevel>
)