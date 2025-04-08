package co.nilin.opex.wallet.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.app.dto.SellVoucherRequest
import co.nilin.opex.wallet.core.inout.Deposit
import co.nilin.opex.wallet.core.inout.SubmitVoucherResponse
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
    private val depositPersister: DepositPersister,
) {
    private val logger = LoggerFactory.getLogger(VoucherService::class.java)

    @Transactional
    suspend fun submitVoucher(uuid: String, code: String): SubmitVoucherResponse {
        logger.info("Submitting voucher for user: $uuid with code: $code")
        val voucher = findAndValidateVoucher(code, uuid)
        voucherManager.saveVoucherUsage(requireNotNull(voucher.id), uuid)
        val transferRef = "wallet:voucher:" + UUID.randomUUID().toString()
        executeTransfer(voucher, uuid, transferRef)
        persistDeposit(voucher, uuid, transferRef)
        logger.info("Voucher submitted successfully for user: $uuid with transfer reference: $transferRef")
        return SubmitVoucherResponse(
            voucher.amount,
            voucher.currency,
            voucher.voucherGroup.issuer,
            voucher.voucherGroup.description
        )
    }

    suspend fun getVoucher(publicCode: String): VoucherData {
        return voucherManager.getVoucherDataByPublicCode(publicCode)
    }

    suspend fun getVouchers(type: VoucherGroupType?, limit: Int?, offset: Int?): List<VoucherData> {
        return voucherManager.getVouchers(type, limit, offset)
    }

    suspend fun sellVoucher(request: SellVoucherRequest, uuid: String) {
        val voucher = voucherManager.getVoucherByPublicCode(request.publicCode)
        val voucherId = requireNotNull(voucher.id) { "Voucher ID cannot be null" }

        if (voucher.voucherGroup.type != VoucherGroupType.SALE || voucherManager.getUsageCount(voucherId) > 0)
            throw OpexError.InvalidVoucher.exception("This voucher cannot be sold")

        voucherManager.saveVoucherSaleData(
            VoucherSaleData(
                voucherId,
                request.nationalCode,
                request.phoneNumber,
                request.transactionNumber,
                request.transactionAmount,
                LocalDateTime.now(),
                uuid
            )
        )
        logger.info("Voucher with code: ${request.publicCode} sold by $uuid")
    }

    private fun hashWithSHA256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private suspend fun findAndValidateVoucher(code: String, uuid: String): Voucher {
        val voucher = voucherManager.getVoucherByPrivateCode(hashWithSHA256(code))
        val voucherGroup = voucherManager.getVoucherGroup(requireNotNull(voucher.voucherGroup.id))
        validateVoucher(voucher, voucherGroup, uuid)
        return voucher
    }

    private suspend fun validateVoucher(voucher: Voucher, voucherGroup: VoucherGroup, uuid: String) {
        if (voucherGroup.status != VoucherGroupStatus.ACTIVE)
            throw OpexError.VoucherGroupIsInactive.exception()

        if (voucher.expireDate < LocalDateTime.now())
            throw OpexError.VoucherExpired.exception()

        val voucherGroupId = requireNotNull(voucherGroup.id) { "Voucher group ID cannot be null" }
        val voucherId = requireNotNull(voucher.id) { "Voucher ID cannot be null" }

        validateUserLimit(voucherGroup, uuid, voucherGroupId)

        when (voucherGroup.type) {
            VoucherGroupType.GIFT -> validateGiftVoucher(voucherId)
            VoucherGroupType.CAMPAIGN -> validateCampaignVoucher(voucherGroup)
            VoucherGroupType.SALE -> validateSaleVoucher(voucherId)
            else -> throw OpexError.BadRequest.exception("Unsupported voucher group type: ${voucherGroup.type}")
        }
    }

    private suspend fun validateUserLimit(group: VoucherGroup, uuid: String, groupId: Long) {
        group.userLimit?.let { limit ->
            val usageCount = voucherManager.getUsageCount(uuid, groupId)
            if (usageCount >= limit) {
                throw OpexError.InvalidVoucher.exception("Voucher usage limit exceeded for user")
            }
        }
    }

    private suspend fun validateGiftVoucher(voucherId: Long) {
        if (voucherManager.isExistVoucherUsage(voucherId)) {
            throw OpexError.VoucherAlreadyUsed.exception()
        }
    }

    private suspend fun validateCampaignVoucher(group: VoucherGroup) {
        val remainingUsage = group.remainingUsage
            ?: throw OpexError.InvalidVoucher.exception("Campaign voucher is not available")

        if (remainingUsage < 1) {
            throw OpexError.InvalidVoucher.exception("No remaining usage for this campaign voucher")
        }
    }

    private suspend fun validateSaleVoucher(voucherId: Long) {
        if (voucherManager.isExistVoucherUsage(voucherId)) {
            throw OpexError.VoucherAlreadyUsed.exception()
        }

        if (!voucherManager.isExistVoucherSaleData(voucherId)) {
            throw OpexError.InvalidVoucher.exception("Voucher sale data not found")
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
            description = voucher.voucherGroup.description,
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