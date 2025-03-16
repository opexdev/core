package co.nilin.opex.otp.app.repository

import co.nilin.opex.otp.app.model.OTP
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface OTPRepository : ReactiveCrudRepository<OTP, Long> {


}