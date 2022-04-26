package co.nilin.opex.referral.app.controller

import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.referral.core.spi.CommissionRewardHandler
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.ZoneId
import java.util.*

@RestController
class CommissionController(private val commissionRewardHandler: CommissionRewardHandler) {
    data class CommissionRewardBody(
        var rewardedUuid: String,
        var referentUuid: String,
        var referralCode: String,
        var richTrade: Long,
        var referentOrderDirection: OrderDirection,
        var share: BigDecimal,
        var createDate: Date
    )

    @ApiOperation(
        value = "Get all commissions",
        notes = "Get all commissions by referer or referent."
    )
    @ApiResponse(
        message = "OK",
        code = 200,
        response = CommissionRewardBody::class,
        responseContainer = "List",
        examples = Example(
            ExampleProperty(
                mediaType = "application/json",
                value = """
[
    {
        "rewardedUuid": "b3e4f2bd-15c6-4912-bdef-161445a98193",
        "referentUuid": "a5e510f9-bda8-4ecb-b500-0980f525dc52",
        "referralCode": "10000",
        "richTrade": 1,
        "referentOrderDirection": "BID",
        "share": 0.01,
        "createDate": 1646213088
    }
]
                """
            )
        )
    )
    @GetMapping("/commissions", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getCommissions(
        @RequestParam(required = false) code: String?,
        @RequestParam(required = false) rewardedUuid: String?,
        @RequestParam(required = false) referentUuid: String?
    ): List<CommissionRewardBody> {
        return commissionRewardHandler.findCommissions(
            referralCode = code,
            referentUuid = referentUuid,
            rewardedUuid = rewardedUuid
        ).map {
            CommissionRewardBody(
                it.rewardedUuid,
                it.referentUuid,
                it.referralCode,
                it.richTrade.first,
                it.referentOrderDirection,
                it.share,
                Date.from(it.createDate.atZone(ZoneId.systemDefault()).toInstant())
            )
        }
    }

    @ApiOperation(
        value = "Batch delete commissions",
        notes = "Delete commissions base on given information."
    )
    @ApiResponse(message = "OK", code = 200)
    @DeleteMapping("/commissions", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun deleteCommissions(
        @RequestParam(required = false) code: String?,
        @RequestParam(required = false) referrerUuid: String?,
        @RequestParam(required = false) referentUuid: String?
    ) {
        commissionRewardHandler.deleteCommissions(code, referrerUuid, referentUuid)
    }

    @ApiOperation(
        value = "Delete commission record",
        notes = "Delete commission record by id."
    )
    @ApiResponse(message = "OK", code = 200)
    @DeleteMapping("/commissions/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun deleteCommissionById(@PathVariable id: Long) {
        commissionRewardHandler.deleteCommissionById(id)
    }
}
