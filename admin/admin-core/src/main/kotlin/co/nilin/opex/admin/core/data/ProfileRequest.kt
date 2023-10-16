package co.nilin.opex.admin.core.data

import java.time.LocalDateTime

data class ProfileRequest(
        var userId:String?,
        var mobile:String?,
        var email:String?,
        var nationalCode:String?,
        var firstName:String?,
        var lastName:String?,
        var createDateFrom:LocalDateTime?,
        var createDateTo:LocalDateTime?,
        var includeKyc:Boolean?,
        var includeLimitation:Boolean?,
        var includeLinkedAccount:Boolean?,
        var accountNumber:String?,
        var partialSearch:Boolean?
)
