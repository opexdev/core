package co.nilin.opex.utility.error.data

import org.springframework.http.HttpStatus

enum class OpexError(val code: Int, val message: String?, val status: HttpStatus) {

    // Code 1000: general
    Error(1000, "Generic error", HttpStatus.INTERNAL_SERVER_ERROR),
    InternalServerError(1001, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    BadRequest(1002, "Bad request", HttpStatus.BAD_REQUEST),
    UnAuthorized(1003, "Unauthorized", HttpStatus.UNAUTHORIZED),
    Forbidden(1004, "Forbidden", HttpStatus.FORBIDDEN),
    NotFound(1005, "Not found", HttpStatus.NOT_FOUND),
    InvalidRequestParam(1020, "Parameter '%s' is either missing or invalid", HttpStatus.BAD_REQUEST),

    // code 2000: accountant
    InvalidPair(2001, "%s is not available", HttpStatus.BAD_REQUEST),
    InvalidPairFee(2002, "%s fee is not available", HttpStatus.BAD_REQUEST),

    // code 3000: matching-engine

    // code 4000: matching-gateway
    SubmitOrderForbiddenByAccountant(4001, null, HttpStatus.BAD_REQUEST),

    // code 5000: user-management

    // code 6000: wallet
    WalletOwnerNotFound(6001, null, HttpStatus.NOT_FOUND),
    WalletNotFound(6002, null, HttpStatus.NOT_FOUND),
    CurrencyNotFound(6003, null, HttpStatus.NOT_FOUND),
    InvalidCashOutUsage(6004, "Use withdraw services", HttpStatus.BAD_REQUEST),

    // code 7000: api
    OrderNotFound(7001, "No order found", HttpStatus.NOT_FOUND),
    SymbolNotFound(7002, "No symbol found", HttpStatus.NOT_FOUND),
    InvalidLimitForOrderBook(7003, "Valid limits: [5, 10, 20, 50, 100, 500, 1000, 5000]", HttpStatus.BAD_REQUEST),
    InvalidLimitForRecentTrades(7004, "Valid limits: 1 min - 1000 max", HttpStatus.BAD_REQUEST),
    InvalidPriceChangeDuration(7005, "Valid durations: [24h, 7d, 1m]", HttpStatus.BAD_REQUEST),
    CancelOrderNotAllowed(7006, "Canceling this order is not allowed", HttpStatus.FORBIDDEN),
    InvalidInterval(7007, "Invalid interval", HttpStatus.BAD_REQUEST),

    // code 8000: bc-gateway
    ReservedAddressNotAvailable(8001, "No reserved address available", HttpStatus.BAD_REQUEST);

    companion object {
        fun findByCode(code: Int?): OpexError? {
            code ?: return null
            return values().find { it.code == code }
        }
    }

}

@Throws(OpexException::class)
inline fun <reified T : Any> T.throwError(
    error: OpexError,
    message: String? = null,
    status: HttpStatus? = null,
    data: Any? = null
) {
    throw OpexException(error, message, status, data, T::class.java)
}