package co.nilin.opex.utility.preferences

data class ProjectPreferences(
    var addressTypes: List<AddressType> = emptyList(),
    var chains: List<Chain> = emptyList(),
    var currencies: List<Currency> = emptyList(),
    var markets: List<Market> = emptyList(),
    var userLimits: List<UserLimit> = emptyList(),
    var systemWallet: SystemWallet = SystemWallet()
)
