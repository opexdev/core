package co.nilin.opex.api.core.inout

data class UserLevelConfig(
    val userLevel: String,
    val language: String,
    val name: String,
    val description: String? = null,
    val permissions: Permissions
)

data class Permissions(
    val onChainDepositAllowed: Boolean? = false,
    val offChainDepositAllowed: Boolean? = false,
    val onChainWithdrawAllowed: Boolean? = false,
    val offChainWithdrawAllowed: Boolean? = false
)