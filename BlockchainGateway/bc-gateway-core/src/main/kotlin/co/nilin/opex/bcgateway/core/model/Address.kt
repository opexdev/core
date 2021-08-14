package co.nilin.opex.bcgateway.core.model

data class AddressType(val id: Long, val type: String, val addressRegex: String, val memoRegex: String)
data class CachedAddress(val address: String, val memo: String?, val type: AddressType)
data class AssignedAddress(val uuid: String, val address: String, val memo: String?, val type: AddressType, val chains: MutableList<Chain> ){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AssignedAddress

        if (uuid != other.uuid) return false
        if (address != other.address) return false
        if (memo != other.memo) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + (memo?.hashCode() ?: 0)
        result = 31 * result + type.hashCode()
        return result
    }
}