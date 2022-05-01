package co.nilin.opex.admin.ports.auth.data

enum class KycGroup(val groupName: String) {

    REQUESTED("kyc-requested"),
    ACCEPTED("kyc-accepted"),
    REJECTED("kyc-rejected"),
    BLOCKED("kyc-blocked")

}