package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.model.WithdrawOtp
import co.nilin.opex.wallet.core.spi.WithdrawOtpPersister
import co.nilin.opex.wallet.ports.postgres.dao.WithdrawOtpRepository
import co.nilin.opex.wallet.ports.postgres.model.WithdrawOtpModel
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.stereotype.Service

@Service
class WithdrawOtpPersisterImpl(private val withdrawOtpRepository: WithdrawOtpRepository) : WithdrawOtpPersister {

    override suspend fun save(withdrawOtp: WithdrawOtp) {
        withdrawOtpRepository.save(withdrawOtp.asWithdrawOtpModel()).awaitFirst()
    }

    override suspend fun findByWithdrawId(withdrawId: Long): List<WithdrawOtp> {
        return withdrawOtpRepository.findByWithdrawId(withdrawId)
            .map { it.asWithdrawOtp() }
            .collectList()
            .awaitFirst()
    }

    private fun WithdrawOtp.asWithdrawOtpModel(): WithdrawOtpModel {
        return WithdrawOtpModel(
            null,
            withdraw,
            otpTracingCode,
            otpType,
            createDate
        )
    }

    private fun WithdrawOtpModel.asWithdrawOtp(): WithdrawOtp {
        return WithdrawOtp(
            withdraw,
            otpTracingCode,
            otpType,
            createDate
        )
    }
}