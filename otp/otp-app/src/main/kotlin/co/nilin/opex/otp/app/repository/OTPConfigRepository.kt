package co.nilin.opex.otp.app.repository

import co.nilin.opex.otp.app.model.OTPConfig
import co.nilin.opex.otp.app.model.OTPType
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface OTPConfigRepository : ReactiveCrudRepository<OTPConfig, OTPType>