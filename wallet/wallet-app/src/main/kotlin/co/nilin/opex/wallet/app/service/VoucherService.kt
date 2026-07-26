package co.nilin.opex.wallet.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.app.dto.SellVoucherRequest
import co.nilin.opex.wallet.app.dto.VoucherSaleDataResponse
import co.nilin.opex.wallet.app.dto.VoucherUsageDataResponse
import co.nilin.opex.wallet.app.utils.asDate
import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.model.DepositType
import co.nilin.opex.wallet.core.service.GatewayService
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
    private val depositService: DepositService,
    private val gatewayService: GatewayService,
) {
    private val logger = LoggerFactory.getLogger(VoucherService::class.java)

    @Transactional
    suspend fun submitVoucher(uuid: String, code: String): SubmitVoucherResponse {
        logger.info("Submitting voucher for user: $uuid with code: $code")
        val voucher = findAndValidateVoucher(code, uuid)
        updateVoucherGroupRemaining(voucher.voucherGroup)
        voucherManager.saveVoucherUsage(requireNotNull(voucher.id), uuid)
        deposit(voucher, uuid)
        logger.info("Voucher ${voucher.publicCode} submitted successfully for user: $uuid")
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

    suspend fun getVouchers(
        type: VoucherGroupType?,
        issuer: String?,
        isUsed: Boolean?,
        limit: Int?,
        offset: Int?,
    ): List<VoucherData> {
        return voucherManager.getVouchers(type, issuer, isUsed, limit, offset)
    }

    suspend fun sellVoucher(request: SellVoucherRequest, uuid: String) {
        val voucher = voucherManager.getVoucherByPublicCode(request.publicCode)
        val voucherId = requireNotNull(voucher.id) { "Voucher ID cannot be null" }

        if (voucher.voucherGroup.type != VoucherGroupType.SALE || voucherManager.getUsageCount(voucherId) > 0)
            throw OpexError.VoucherNotForSale.exception()

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

    suspend fun getVoucherSaleData(publicCode: String): VoucherSaleDataResponse {
        val voucher = voucherManager.getVoucherByPublicCode(publicCode)
        val voucherId = requireNotNull(voucher.id) { "Voucher ID cannot be null" }

        if (voucher.voucherGroup.type != VoucherGroupType.SALE)
            throw OpexError.VoucherNotForSale.exception()

        val voucherSaleData = voucherManager.getVoucherSaleData(voucherId)

        return VoucherSaleDataResponse(
            publicCode,
            voucherSaleData.nationalCode,
            voucherSaleData.phoneNumber,
            voucherSaleData.transactionNumber,
            voucherSaleData.transactionAmount,
            voucherSaleData.saleDate?.asDate(),
            voucherSaleData.sellerUuid
        )
    }

    suspend fun getVoucherUsageData(publicCode: String): List<VoucherUsageDataResponse> {
        val voucher = voucherManager.getVoucherByPublicCode(publicCode)
        val voucherId = requireNotNull(voucher.id) { "Voucher ID cannot be null" }

        val usageData = voucherManager.getVoucherUsageData(voucherId)
        return usageData.map { usage ->
            VoucherUsageDataResponse(usage.useDate.asDate(), usage.uuid)
        }
    }

    private suspend fun updateVoucherGroupRemaining(voucherGroup: VoucherGroup) {
        if (voucherGroup.type == VoucherGroupType.CAMPAIGN || voucherGroup.remainingUsage != null)
            voucherManager.updateVoucherGroupRemaining(
                requireNotNull(voucherGroup.id),
                requireNotNull(voucherGroup.remainingUsage) - 1
            )
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
                throw OpexError.VoucherUsageLimitExceeded.exception("Voucher usage limit exceeded for user")
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
            ?: throw OpexError.VoucherUsageLimitExceeded.exception("Campaign voucher is not available")

        if (remainingUsage < 1) {
            throw OpexError.VoucherUsageLimitExceeded.exception("No remaining usage for this campaign voucher")
        }
    }

    private suspend fun validateSaleVoucher(voucherId: Long) {
        if (voucherManager.isExistVoucherUsage(voucherId)) {
            throw OpexError.VoucherAlreadyUsed.exception()
        }

        if (!voucherManager.isExistVoucherSaleData(voucherId)) {
            throw OpexError.VoucherSaleDataNotFound.exception("Voucher sale data not found")
        }
    }

    private suspend fun deposit(voucher: Voucher, uuid: String) {
        val transferRef = "wallet:voucher:" + UUID.randomUUID().toString()
        logger.info("Executing deposit for voucher: ${voucher.publicCode} to user: $uuid with reference: $transferRef")

        val gatewayUuid = gatewayService
            .fetchGateways(voucher.currency, listOf(GatewayType.OffChain))
            ?.find { it is OffChainGatewayCommand && it.transferMethod == TransferMethod.VOUCHER }
            ?.gatewayUuid
            ?: throw OpexError.GatewayNotFount.exception()

        depositService.deposit(
            voucher.currency,
            uuid,
            WalletType.MAIN,
            null,
            voucher.amount,
            "VOUCHER:${voucher.publicCode}",
            transferRef,
            null,
            null,
            DepositType.OFF_CHAIN,
            gatewayUuid,
            TransferMethod.VOUCHER
        )
    }
}