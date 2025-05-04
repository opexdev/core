package co.nilin.opex.common

import co.nilin.opex.utility.error.data.ErrorRep
import co.nilin.opex.utility.error.data.OpexException
import org.springframework.http.HttpStatus

enum class OpexError(val code: Int, val message: String?, val status: HttpStatus) : ErrorRep {

    // Code 1000: general
    Error(1000, "Generic error", HttpStatus.INTERNAL_SERVER_ERROR),
    InternalServerError(1001, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    BadRequest(1002, "Bad request", HttpStatus.BAD_REQUEST),
    UnAuthorized(1003, "Unauthorized", HttpStatus.UNAUTHORIZED),
    Forbidden(1004, "Forbidden", HttpStatus.FORBIDDEN),
    NotFound(1005, "Not found", HttpStatus.NOT_FOUND),
    ServiceUnavailable(1006, null, HttpStatus.SERVICE_UNAVAILABLE),
    InvalidRequestParam(1020, "Parameter '%s' is either missing or invalid", HttpStatus.BAD_REQUEST),
    InvalidRequestBody(1021, "Request body is invalid", HttpStatus.BAD_REQUEST),
    NoRecordFound(1022, "No record found for this service", HttpStatus.NOT_FOUND),
    ServiceDeprecated(1023, "Service deprecated", HttpStatus.SERVICE_UNAVAILABLE),

    // code 2000: accountant
    InvalidPair(2001, "%s is not available", HttpStatus.BAD_REQUEST),
    InvalidPairFee(2002, "%s fee is not available", HttpStatus.BAD_REQUEST),
    PairFeeNotFound(2002, "No fee for requested pair found", HttpStatus.NOT_FOUND),

    // code 3000: matching-engine

    // code 4000: matching-gateway
    SubmitOrderForbiddenByAccountant(4001, null, HttpStatus.BAD_REQUEST),

    // code 5000: user-management
    EmailAlreadyVerified(5001, "Email is already verified", HttpStatus.BAD_REQUEST),
    GroupNotFound(5002, "Group not found", HttpStatus.NOT_FOUND),
    OTPAlreadyEnabled(5003, "2FA/OTP already configured", HttpStatus.BAD_REQUEST),
    UserNotFound(5004, "User not found", HttpStatus.NOT_FOUND),
    InvalidOTP(5005, "Invalid OTP", HttpStatus.FORBIDDEN),
    OTPRequired(5006, "OTP Required", HttpStatus.BAD_REQUEST),
    AlreadyInKYC(5007, "KYC flow for this user has executed", HttpStatus.BAD_REQUEST),
    UserKYCBlocked(5008, "User is blocked from KYC", HttpStatus.BAD_REQUEST),
    InvalidPassword(5009, "Password is not valid", HttpStatus.BAD_REQUEST),
    UserAlreadyExists(5009, "User is already registered", HttpStatus.BAD_REQUEST),
    LoginIsLimited(5010, "Your email is not in whitelist", HttpStatus.BAD_REQUEST),
    RegisterIsLimited(5011, "Your email is not in whitelist", HttpStatus.BAD_REQUEST),
    GmailNotFoundInToken(5012, "Email not found in Google token", HttpStatus.NOT_FOUND),
    UserIDNotFoundInToken(5013, "Google user ID (sub) not found in token", HttpStatus.NOT_FOUND),
    InvalidUsername(5014, "Invalid username", HttpStatus.BAD_REQUEST),
    InvalidUserCredentials(5015, "Invalid user credentials", HttpStatus.BAD_REQUEST),


    // code 6000: wallet
    WalletOwnerNotFound(6001, null, HttpStatus.NOT_FOUND),
    WalletNotFound(6002, null, HttpStatus.NOT_FOUND),
    CurrencyNotFound(6003, null, HttpStatus.NOT_FOUND),
    InvalidCashOutUsage(6004, "Use withdraw services", HttpStatus.BAD_REQUEST),
    WithdrawNotFound(6005, "No matching withdraw request", HttpStatus.NOT_FOUND),
    NOT_EXCHANGEABLE_CURRENCIES(6006, "These two currencies can't be exchanged", HttpStatus.NOT_FOUND),
    CurrencyIsExist(6007, null, HttpStatus.BAD_REQUEST),
    PairIsExist(6008, null, HttpStatus.BAD_REQUEST),
    ForbiddenPair(6009, null, HttpStatus.BAD_REQUEST),
    InvalidRate(6010, null, HttpStatus.BAD_REQUEST),
    PairNotFound(6011, null, HttpStatus.BAD_REQUEST),
    SourceIsEqualDest(6012, null, HttpStatus.BAD_REQUEST),
    AtLeastNeedOneTransitiveSymbol(6013, null, HttpStatus.BAD_REQUEST),
    CurrencyIsDisable(6014, null, HttpStatus.BAD_REQUEST),
    CurrencyIsTransitiveAndDisablingIsImpossible(6015, null, HttpStatus.BAD_REQUEST),
    InvalidReserveNumber(6016, null, HttpStatus.BAD_REQUEST),
    CurrentSystemAssetsAreNotEnough(6017, null, HttpStatus.INTERNAL_SERVER_ERROR),
    NotEnoughBalance(6018, null, HttpStatus.BAD_REQUEST),
    WithdrawNotAllowed(6019, null, HttpStatus.BAD_REQUEST),
    DepositLimitExceeded(6020, "Deposit limit exceeded", HttpStatus.BAD_REQUEST),
    InvalidAmount(6021, "Invalid amount", HttpStatus.BAD_REQUEST),
    WithdrawAlreadyProcessed(6022, "This withdraw request processed before", HttpStatus.BAD_REQUEST),
    InvalidAppliedFee(6023, "Applied fee is bigger than accepted fee", HttpStatus.BAD_REQUEST),
    WithdrawAmountExceedsWalletBalance(6024, "Withdraw amount exceeds wallet balance", HttpStatus.BAD_REQUEST),
    WithdrawAmountLessThanMinimum(6025, "Withdraw amount is less than minimum", HttpStatus.BAD_REQUEST),
    WithdrawCannotBeCanceled(6026, "Withdraw cannot be canceled", HttpStatus.BAD_REQUEST),
    WithdrawCannotBeRejected(6027, "Withdraw cannot be rejected", HttpStatus.BAD_REQUEST),
    WithdrawAmountMoreThanMinimum(6028, "Withdraw amount is more than minimum", HttpStatus.BAD_REQUEST),
    ImplNotFound(6029, null, HttpStatus.NOT_FOUND),
    InvalidWithdrawStatus(6030, "Withdraw status is invalid", HttpStatus.NOT_FOUND),
    GatewayNotFount(6031, null, HttpStatus.NOT_FOUND),
    GatewayIsExist(6032, null, HttpStatus.NOT_FOUND),
    InvalidDeposit(6033, "Invalid deposit", HttpStatus.BAD_REQUEST),
    TerminalIsExist(6034, "This identifier is exist", HttpStatus.BAD_REQUEST),
    TerminalNotFound(6035, "Object not found", HttpStatus.BAD_REQUEST),
    VoucherNotFound(6036, "Voucher not found", HttpStatus.NOT_FOUND),
    InvalidVoucher(6037, "Invalid Voucher", HttpStatus.BAD_REQUEST),
    PairIsNotAvailable(6038, "Pair is not available", HttpStatus.BAD_REQUEST),


    // code 7000: api
    OrderNotFound(7001, "No order found", HttpStatus.NOT_FOUND),
    SymbolNotFound(7002, "No symbol found", HttpStatus.NOT_FOUND),
    InvalidLimitForOrderBook(7003, "Valid limits: [5, 10, 20, 50, 100, 500, 1000, 5000]", HttpStatus.BAD_REQUEST),
    InvalidLimitForRecentTrades(7004, "Valid limits: 1 min - 1000 max", HttpStatus.BAD_REQUEST),
    InvalidPriceChangeDuration(7005, "Valid durations: [24h, 7d, 1m]", HttpStatus.BAD_REQUEST),
    CancelOrderNotAllowed(7006, "Canceling this order is not allowed", HttpStatus.FORBIDDEN),
    InvalidInterval(7007, "Invalid interval", HttpStatus.BAD_REQUEST),
    APIKeyLimitReached(7007, "Reached API key limit. Maximum number of API key is 10", HttpStatus.BAD_REQUEST),

    // code 8000: bc-gateway
    ReservedAddressNotAvailable(8001, "No reserved address available", HttpStatus.BAD_REQUEST),
    DuplicateToken(8002, "Asset already exists", HttpStatus.BAD_REQUEST),
    ChainNotFound(8003, "Chain not found", HttpStatus.NOT_FOUND),
    CurrencyNotFoundBC(8004, "Currency not found", HttpStatus.NOT_FOUND),
    TokenNotFound(8005, "Coin/Token not found", HttpStatus.NOT_FOUND),
    InvalidAddressType(8006, "Address type is invalid", HttpStatus.NOT_FOUND),

    // code 10000: bc-gateway
    InvalidCaptcha(10001, "Captcha is not valid", HttpStatus.BAD_REQUEST),

    // code 11000: market

    // code 12000: otp
    OTPConfigNotFound(12001, "Config for otp type not found", HttpStatus.NOT_FOUND),
    UnableToSendOTP(12002, "Unable to send OTP code to the receiver", HttpStatus.INTERNAL_SERVER_ERROR),
    OTPAlreadyRequested(12003, "OTP code is already requested for the receiver and OTP type", HttpStatus.BAD_REQUEST),
    TOTPNotFound(12004, "TOTP for the requested user not found", HttpStatus.NOT_FOUND),
    InvalidTOTPCode(12005, "TOTP code is invalid", HttpStatus.BAD_REQUEST),
    TOTPSetupIncomplete(12006, "TOTP setup is incomplete", HttpStatus.BAD_REQUEST),
    TOTPAlreadyRegistered(12007, "User already registered for TOTP", HttpStatus.BAD_REQUEST),

    ;

    override fun code() = this.code

    override fun message() = this.message

    override fun status() = this.status

    override fun errorName() = this.name

    fun exception(message: String? = null): OpexException {
        return OpexException(this, message = message)
    }

    fun messageFormattedException(vararg params: Any): OpexException {
        return OpexException(this, String.format(message!!, params))
    }

    companion object {
        fun findByCode(code: Int?): OpexError? {
            code ?: return null
            return values().find { it.code == code }
        }
    }

}