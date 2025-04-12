package co.nilin.opex.otp.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.otp.app.model.TOTP
import co.nilin.opex.otp.app.repository.TOTPConfigRepository
import co.nilin.opex.otp.app.repository.TOTPRepository
import dev.samstevens.totp.code.DefaultCodeGenerator
import dev.samstevens.totp.code.DefaultCodeVerifier
import dev.samstevens.totp.code.HashingAlgorithm
import dev.samstevens.totp.qr.QrData
import dev.samstevens.totp.secret.DefaultSecretGenerator
import dev.samstevens.totp.time.SystemTimeProvider
import org.springframework.stereotype.Service

@Service
class TOTPService(
    private val repository: TOTPRepository,
    private val configRepository: TOTPConfigRepository
) {

    suspend fun setupTOTP(userId: String, label: String? = null): String {
        val config = configRepository.findOne()
        repository.findByUserId(userId)?.let { throw OpexError.TOTPAlreadyRegistered.exception() }
        val secret = generateSecret()
        val uri = generateUri(userId, config.issuer, secret, label)
        repository.save(TOTP(userId, secret, label))
        return uri
    }

    suspend fun verifyAndMarkActivated(userId: String, code: String) {
        val totp = repository.findByUserId(userId) ?: throw OpexError.TOTPNotFound.exception()
        val isValid = isCodeValid(totp.secret, code.trim())
        if (isValid)
            repository.markActivated(totp.id!!)
        else
            throw OpexError.InvalidTOTPCode.exception()
    }

    suspend fun verifyTOTP(userId: String, code: String): Boolean {
        val totp = repository.findByUserId(userId) ?: throw OpexError.TOTPNotFound.exception()
        if (!totp.isActivated) throw OpexError.TOTPSetupIncomplete.exception()
        return isCodeValid(totp.secret, code.trim())
    }

    suspend fun removeTOTP(userId: String, code: String) {
        val totp = repository.findByUserId(userId) ?: throw OpexError.TOTPNotFound.exception()
        if (!totp.isActivated)
            repository.deleteById(totp.id!!)
        else {
            val isValid = isCodeValid(totp.secret, code.trim())
            if (isValid)
                repository.deleteById(totp.id!!)
            else
                throw OpexError.InvalidTOTPCode.exception()
        }
    }

    private suspend fun generateSecret(): String {
        val config = configRepository.findOne()
        val generator = DefaultSecretGenerator(config.secretChars)
        return generator.generate()
    }

    private fun generateUri(userId: String, issuer: String, secret: String, label: String? = null): String {
        val data = QrData.Builder()
            .label(label ?: userId)
            .secret(secret)
            .issuer(issuer)
            .algorithm(HashingAlgorithm.SHA1)
            .digits(6)
            .period(30)
            .build()
        return data.uri
    }

    private fun isCodeValid(secret: String, code: String): Boolean {
        val timeProvider = SystemTimeProvider()
        val generator = DefaultCodeGenerator(HashingAlgorithm.SHA1, 6)
        val verifier = DefaultCodeVerifier(generator, timeProvider).apply {
            setTimePeriod(30)
            setAllowedTimePeriodDiscrepancy(3)
        }
        return verifier.isValidCode(secret, code)
    }
}