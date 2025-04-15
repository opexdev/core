package co.nilin.opex.profile.core.data.profile

import java.time.LocalDateTime

data class ProfileRequest(var userId: String?,
                          var mobile: String?,
                          var email: String?,
                          var linkedAccount: String?,
                          var nationalCode: String?,
                          var firstName: String?,
                          var lastName: String?,
                          var createDateFrom: LocalDateTime?,
                          var accountNumber: String?,
                          var createDateTo: LocalDateTime?,
                          var includeLimitation: Boolean?,
                          var includeLinkedAccount: Boolean?,
                          var partialSearch: Boolean? = false)
