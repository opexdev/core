package co.nilin.opex.profile.ports.postgres.model.entity

import co.nilin.opex.profile.ports.postgres.model.base.ProfileApprovalRequest
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("profile_approval_request")
data class ProfileApprovalRequestModel(
    @Id var id: Long
) : ProfileApprovalRequest()