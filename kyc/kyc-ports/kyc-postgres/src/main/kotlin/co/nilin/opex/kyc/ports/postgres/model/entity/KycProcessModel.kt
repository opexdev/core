package co.nilin.opex.kyc.ports.postgres.model.entity

import co.nilin.opex.kyc.ports.postgres.model.base.KycProcess
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("kyc_process")
data class KycProcessModel(
        @Id
        var id: Long
) : KycProcess()
