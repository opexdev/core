package co.nilin.opex.profile.core.spi

interface AccessManagement {
    fun grantPermission()
    fun revokePermission()
}