package co.nilin.opex.api.core.inout

data class AssignAddressRequest(val uuid: String, val currency: String, val gatewayUuid: String)