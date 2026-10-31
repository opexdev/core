package co.nilin.opex.profile.ports.postgres.imp

import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.core.data.profile.BankAccountApprovalAdminResponse
import co.nilin.opex.profile.core.data.profile.BankAccountApprovalRequest
import co.nilin.opex.profile.core.data.profile.BankAccountApprovalRequestFilter
import co.nilin.opex.profile.core.data.profile.BankAccountApprovalUserResponse
import co.nilin.opex.profile.core.spi.BankAccountApprovalRequestPersister
import co.nilin.opex.profile.core.utils.convert
import co.nilin.opex.profile.ports.postgres.dao.BankAccountApprovalRequestRepository
import co.nilin.opex.profile.ports.postgres.model.entity.BankAccountApprovalRequestModel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class BankAccountApprovalRequestManagementImp(
    private val bankAccountApprovalRequestRepository: BankAccountApprovalRequestRepository,
) : BankAccountApprovalRequestPersister {

    override suspend fun save(request: BankAccountApprovalRequest): BankAccountApprovalRequest {
        bankAccountApprovalRequestRepository.findByBankAccountId(request.bankAccountId)
            .awaitFirstOrNull()?.let {
                throw OpexError.BankAccountApprovalRequestAlreadyExists.exception()
            } ?: run {
            val model: BankAccountApprovalRequestModel =
                request.convert(BankAccountApprovalRequestModel::class.java)
            return bankAccountApprovalRequestRepository.save(model).awaitFirst()
                .convert(BankAccountApprovalRequest::class.java)
        }
    }

    override suspend fun getRequests(
        request: BankAccountApprovalRequestFilter
    ): List<BankAccountApprovalAdminResponse> {
        val createDateFrom = request.createDateFrom?.toLocalDateTime()
        val createDateTo = request.createDateTo?.toLocalDateTime()

        val approvalRequests =
            if (request.ascendingByTime)
                bankAccountApprovalRequestRepository.findByCriteriaAsc(
                    request.userId,
                    request.status,
                    createDateFrom,
                    createDateTo,
                    request.limit,
                    request.offset
                ) else
                bankAccountApprovalRequestRepository.findByCriteriaDesc(
                    request.userId,
                    request.status,
                    createDateFrom,
                    createDateTo,
                    request.limit,
                    request.offset
                )
        return approvalRequests.toList()
    }

    override suspend fun getRequestById(id: Long): BankAccountApprovalAdminResponse {
        return bankAccountApprovalRequestRepository.findAdminResponseById(id).awaitFirstOrNull()
            ?: throw OpexError.BankAccountApprovalRequestNotFound.exception()
    }

    override suspend fun getRequestByBankAccountId(bankAccountId: Long): BankAccountApprovalUserResponse {
        return bankAccountApprovalRequestRepository.findUserResponseByBankAccountId(bankAccountId)
            .awaitFirstOrNull()
            ?: throw OpexError.BankAccountApprovalRequestNotFound.exception()
    }

    override suspend fun update(request: BankAccountApprovalAdminResponse): BankAccountApprovalAdminResponse {
        val model: BankAccountApprovalRequestModel =
            request.convert(BankAccountApprovalRequestModel::class.java)
        bankAccountApprovalRequestRepository.save(model).awaitFirst()

        return request
    }

    private fun Long.toLocalDateTime(): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
}
