package co.nilin.opex.utility.preferences

data class Preferences(
    var addressTypes: List<AddressType> = emptyList(),
    var chains: List<Chain> = emptyList(),
    var currencies: List<Currency> = emptyList(),
    var markets: List<Market> = emptyList(),
    var userLimits: List<UserLimit> = emptyList(),  
    var userLevels: List<String> = emptyList(),
    var system: System = System(),
    val auth: Auth = Auth()
)
