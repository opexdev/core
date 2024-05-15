package co.nilin.opex.bcgateway.core.model

data class ReservedAddress(val address: String, val memo: String?, val type: AddressType)
data class ReservedAddressV2(val address: String, val memo: String?)
