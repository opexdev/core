package co.nilin.opex.auth.data

data class ActionCache(
    val actionType: ActionType,
    val remainingAttempts: Int,
)

enum class ActionType {
    REGISTER, FORGET, LOGIN
}