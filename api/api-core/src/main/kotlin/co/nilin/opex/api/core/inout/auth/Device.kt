package co.nilin.opex.api.core.inout.auth

open class Device {
    var appVersion: String? = null
    var os: Os? = null
    var osVersion: String? = null
    var brand: String? = null
    var model: String? = null
    var platform: Platform? = null
    var agent: String? = null
    var pushToken: String? = null
    var deviceUuid: String? = null
    var buildNumber: Int? = null
    var ipAddress: String? = null
}