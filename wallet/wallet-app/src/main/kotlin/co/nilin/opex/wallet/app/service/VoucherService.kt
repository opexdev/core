package co.nilin.opex.wallet.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.inout.Deposit
import co.nilin.opex.wallet.core.inout.VoucherData
import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.spi.DepositPersister
import co.nilin.opex.wallet.core.spi.VoucherManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.*

@Service
class VoucherService(
    private val voucherManager: VoucherManager,
    private val transferService: TransferService,
    private val depositPersister: DepositPersister
) {
    private val logger = LoggerFactory.getLogger(VoucherService::class.java)

    @Transactional
    suspend fun submitVoucher(uuid: String, code: String) {
        logger.info("Submitting voucher for user: $uuid with code: $code")
        val voucher = findAndValidateVoucher(code)
        voucherManager.updateVoucherAsUsed(voucher, uuid)
        val transferRef = UUID.randomUUID().toString()
        executeTransfer(voucher, uuid, transferRef)
        persistDeposit(voucher, uuid, transferRef)
        logger.info("Voucher submitted successfully for user: $uuid with transfer reference: $transferRef")
    }

    suspend fun getVoucher(publicCode: String): VoucherData {
        return voucherManager.findByPublicCode(publicCode) ?: throw OpexError.VoucherNotFound.exception()
    }

    private fun hashWithSHA256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private suspend fun findAndValidateVoucher(code: String): Voucher {
        val voucher = voucherManager.findByPrivateCode(hashWithSHA256(code))
            ?: throw OpexError.VoucherNotFound.exception("Voucher with provided code not found")
        validateVoucher(voucher)
        return voucher
    }

    private fun validateVoucher(voucher: Voucher) {
        if (voucher.status != VoucherStatus.UNUSED) {
            throw OpexError.InvalidVoucher.exception("Voucher has already been used")
        }
        if (voucher.expireDate < LocalDateTime.now()) {
            throw OpexError.InvalidVoucher.exception("Voucher has expired")
        }
    }

    private suspend fun executeTransfer(voucher: Voucher, userId: String, transferRef: String) {
        logger.info("Executing transfer for voucher: ${voucher.publicCode} to user: $userId with reference: $transferRef")
        transferService.transfer(
            symbol = voucher.currency,
            senderWalletType = WalletType.MAIN,
            senderUuid = "1",
            receiverWalletType = WalletType.MAIN,
            receiverUuid = userId,
            amount = voucher.amount,
            description = voucher.voucherGroup?.description,
            transferRef = transferRef,
            transferCategory = TransferCategory.VOUCHER
        )
    }

    private suspend fun persistDeposit(voucher: Voucher, userId: String, transferRef: String) {
        logger.info("Persisting deposit for voucher: ${voucher.publicCode} to user: $userId with reference: $transferRef")
        depositPersister.persist(
            Deposit(
                ownerUuid = userId,
                depositUuid = UUID.randomUUID().toString(),
                currency = voucher.currency,
                amount = voucher.amount,
                note = "VOUCHER",
                transactionRef = transferRef,
                status = DepositStatus.DONE,
                depositType = DepositType.MANUALLY,
                attachment = null,
            )
        )
    }
}