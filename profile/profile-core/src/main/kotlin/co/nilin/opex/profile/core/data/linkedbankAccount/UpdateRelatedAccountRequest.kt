package co.nilin.opex.profile.core.data.linkedbankAccount

data class UpdateRelatedAccountRequest(var userId: String?, var accountId: String?, var status: Status)
enum class Status { Enable, Disable }