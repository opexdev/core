package co.nilin.opex.wallet.core.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.inout.otp.OTPResultType
import co.nilin.opex.wallet.core.inout.otp.OTPType
import co.nilin.opex.wallet.core.inout.otp.OTPVerifyResponse
import co.nilin.opex.wallet.core.inout.otp.TempOtpResponse
import co.nilin.opex.wallet.core.inout.profile.Profile
import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.model.WithdrawType
import co.nilin.opex.wallet.core.spi.*
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class WithdrawServiceTest {
    private val withdrawPersister = mockk<WithdrawPersister>()
    private val walletManager = mockk<WalletManager>()
    private val walletOwnerManager = mockk<WalletOwnerManager>()
    private val currencyService = mockk<CurrencyServiceManager>()
    private val transferManager = mockk<TransferManager>()
    private val meterRegistry = mockk<MeterRegistry>()
    private val gatewayService = mockk<GatewayService>()
    private val precisionService = mockk<PrecisionService>()
    private val accountantProxy = mockk<AccountantProxy>()
    private val withdrawRequestEventSubmitter = mockk<WithdrawRequestEventSubmitter>()
    private val otpProxy = mockk<OtpProxy>()
    private val profileProxy = mockk<ProfileProxy>()
    private val withdrawOtpPersister = mockk<WithdrawOtpPersister>()
    private val gatewayPersister = mockk<GatewayPersister>()

    private lateinit var withdrawService: WithdrawService

    companion object {
        private const val USER_UUID = "user-uuid-123"
        private const val SYSTEM_UUID = "system-uuid"
        private const val TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VySWQiLCJyb2xlcyI6WyJVU0VSIl19.signature" +
                ""
        private const val CURRENCY = "BTC"
        private const val DEST_ADDRESS = "0x123456789"
        private const val DEST_NETWORK = "ETH"
        private const val DEST_SYMBOL = "BTC"
        private const val MOBILE = "09120000000"
        private const val EMAIL = "user@example.com"
        private const val VALID_OTP = "123456"
        private const val INVALID_OTP = "000000"
        private const val WITHDRAW_ID = 1L
        private const val WALLET_ID = 100L
        private val WITHDRAW_AMOUNT = BigDecimal("1.0")
        private val WITHDRAW_FEE = BigDecimal("0.001")
        private val MIN_AMOUNT = BigDecimal("0.01")
        private val MAX_AMOUNT = BigDecimal("10.0")
        private const val DECIMAL = 6

        private fun createCurrency() =
            CurrencyCommand("BTC", UUID.randomUUID().toString(), "Bitcoin", BigDecimal.valueOf(0.0001))

        private fun createOwner() = WalletOwner(1L, USER_UUID, "User", "registered", true, true, true)

        private fun createSystemOwner() = WalletOwner(2L, SYSTEM_UUID, "System", "registered", true, true, true)

        private fun createWallet(owner: WalletOwner, type: WalletType, balance: BigDecimal = BigDecimal.TEN) =
            Wallet(WALLET_ID, owner, Amount(createCurrency(), balance), createCurrency(), type, 1)

        private fun createWithdrawCommand(
            amount: BigDecimal = WITHDRAW_AMOUNT,
            gatewayUuid: String? = "gateway-uuid"
        ) = WithdrawCommand(
            uuid = USER_UUID,
            currency = CURRENCY,
            amount = amount,
            destAddress = DEST_ADDRESS,
            destSymbol = DEST_SYMBOL,
            destNetwork = DEST_NETWORK,
            gatewayUuid = gatewayUuid,
            withdrawType = WithdrawType.ON_CHAIN,
            destNote = "test",
            transferMethod = TransferMethod.EXCHANGE,
        )

        private fun createWithdraw(status: WithdrawStatus = WithdrawStatus.REQUESTED) = Withdraw(
            withdrawId = WITHDRAW_ID,
            ownerUuid = USER_UUID,
            currency = CURRENCY,
            wallet = WALLET_ID,
            amount = WITHDRAW_AMOUNT,
            appliedFee = WITHDRAW_FEE,
            destSymbol = DEST_SYMBOL,
            destAddress = DEST_ADDRESS,
            destNetwork = DEST_NETWORK,
            status = status,
            withdrawType = WithdrawType.ON_CHAIN,
            otpRequired = 2
        )

        private fun createGatewayData() = GatewayData(
            isEnabled = true,
            fee = WITHDRAW_FEE,
            minimum = MIN_AMOUNT,
            maximum = MAX_AMOUNT
        )

        private fun createProfile() = Profile(
            mobile = MOBILE,
            email = EMAIL
        )
    }

    @BeforeEach
    fun setup() {
        clearAllMocks()
        withdrawService = spyk(
            WithdrawService(
                withdrawPersister = withdrawPersister,
                walletManager = walletManager,
                walletOwnerManager = walletOwnerManager,
                currencyService = currencyService,
                transferManager = transferManager,
                meterRegistry = meterRegistry,
                gatewayService = gatewayService,
                precisionService = precisionService,
                accountantProxy = accountantProxy,
                withdrawRequestEventSubmitter = withdrawRequestEventSubmitter,
                otpProxy = otpProxy,
                profileProxy = profileProxy,
                withdrawOtpPersister = withdrawOtpPersister,
                bcGatewayProxy = gatewayPersister,
                systemUuid = SYSTEM_UUID,
                withdrawLimitEnabled = false,
                otpRequiredCount = 0
            )
        )
    }

    @Nested
    inner class RequestWithdrawTests {

        @Test
        fun `should throw CurrencyNotFound when currency does not exist`() = runBlocking {
            val command = createWithdrawCommand()
            coEvery { precisionService.validatePrecision(any(), any()) } returns Unit
            coEvery { currencyService.fetchCurrency(any()) } returns null

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { withdrawService.requestWithdraw(command, TOKEN) }
            }

            Assertions.assertEquals(OpexError.CurrencyNotFound, ex.error)
        }

        @Test
        fun `should throw WalletOwnerNotFound when owner does not exist`() = runBlocking {
            val command = createWithdrawCommand()
            coEvery { precisionService.validatePrecision(any(), any()) } returns Unit
            coEvery { currencyService.fetchCurrency(any()) } returns createCurrency()
            coEvery { walletOwnerManager.findWalletOwner(USER_UUID) } returns null

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { withdrawService.requestWithdraw(command, TOKEN) }
            }

            Assertions.assertEquals(OpexError.WalletOwnerNotFound, ex.error)
        }

        @Test
        fun `should throw GatewayNotFount when gateway data is null`() = runBlocking {
            val command = createWithdrawCommand()
            val owner = createOwner()
            val currency = createCurrency()

            coEvery { precisionService.validatePrecision(any(), any()) } returns Unit
            coEvery { currencyService.fetchCurrency(any()) } returns currency
            coEvery { walletOwnerManager.findWalletOwner(USER_UUID) } returns owner
            coEvery {
                walletManager.findWalletByOwnerAndCurrencyAndType(
                    owner,
                    WalletType.MAIN,
                    currency
                )
            } returns createWallet(owner, WalletType.MAIN)
            coEvery {
                walletManager.findWalletByOwnerAndCurrencyAndType(
                    owner,
                    WalletType.CASHOUT,
                    currency
                )
            } returns createWallet(owner, WalletType.CASHOUT)
            coEvery { gatewayService.fetchGateway(any(), any()) } returns null

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { withdrawService.requestWithdraw(command, TOKEN) }
            }

            Assertions.assertEquals(OpexError.GatewayNotFount, ex.error)
        }

        @Test
        fun `should throw WithdrawNotAllowed when gateway is disabled`() = runBlocking {
            val command = createWithdrawCommand()
            val owner = createOwner()
            val currency = createCurrency()
            val gatewayData = createGatewayData().copy(isEnabled = false)

            coEvery { precisionService.validatePrecision(any(), any()) } returns Unit
            coEvery { currencyService.fetchCurrency(any()) } returns currency
            coEvery { walletOwnerManager.findWalletOwner(USER_UUID) } returns owner
            coEvery {
                walletManager.findWalletByOwnerAndCurrencyAndType(
                    owner,
                    WalletType.MAIN,
                    currency
                )
            } returns createWallet(owner, WalletType.MAIN)
            coEvery {
                walletManager.findWalletByOwnerAndCurrencyAndType(
                    owner,
                    WalletType.CASHOUT,
                    currency
                )
            } returns createWallet(owner, WalletType.CASHOUT)
            coEvery { gatewayService.fetchGateway(any(), any()) } returns OnChainGatewayCommand(
                currencySymbol = CURRENCY,
                implementationSymbol = DEST_SYMBOL,
                chain = DEST_NETWORK,
                decimal = DECIMAL,
                isWithdrawActive = false,
                withdrawAllowed = false,
                withdrawFee = gatewayData.fee,
                withdrawMin = gatewayData.minimum,
                withdrawMax = gatewayData.maximum
            )

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { withdrawService.requestWithdraw(command, TOKEN) }
            }

            Assertions.assertEquals(OpexError.WithdrawNotAllowed, ex.error)
        }

        @Test
        fun `should throw WithdrawAmountExceedsWalletBalance when insufficient balance`() = runBlocking {
            val command = createWithdrawCommand(amount = BigDecimal("100"))
            val owner = createOwner()
            val currency = createCurrency()
            val wallet = createWallet(owner, WalletType.MAIN, BigDecimal.ONE)

            coEvery { precisionService.validatePrecision(any(), any()) } returns Unit
            coEvery { currencyService.fetchCurrency(any()) } returns currency
            coEvery { walletOwnerManager.findWalletOwner(USER_UUID) } returns owner
            coEvery {
                walletManager.findWalletByOwnerAndCurrencyAndType(
                    owner,
                    WalletType.MAIN,
                    currency
                )
            } returns wallet
            coEvery {
                walletManager.findWalletByOwnerAndCurrencyAndType(
                    owner,
                    WalletType.CASHOUT,
                    currency
                )
            } returns createWallet(owner, WalletType.CASHOUT)
            coEvery { gatewayService.fetchGateway(any(), any()) } returns OnChainGatewayCommand(
                currencySymbol = CURRENCY,
                implementationSymbol = DEST_SYMBOL,
                chain = DEST_NETWORK,
                decimal = DECIMAL,
                isWithdrawActive = true,
                withdrawAllowed = true,
                withdrawFee = WITHDRAW_FEE,
                withdrawMin = MIN_AMOUNT,
                withdrawMax = MAX_AMOUNT
            )

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { withdrawService.requestWithdraw(command, TOKEN) }
            }

            Assertions.assertEquals(OpexError.WithdrawAmountExceedsWalletBalance, ex.error)
        }

        @Test
        fun `should throw WithdrawAmountLessThanMinimum when amount is too small`() = runBlocking {
            val command = createWithdrawCommand(amount = BigDecimal("0.001"))
            val owner = createOwner()
            val currency = createCurrency()

            coEvery { precisionService.validatePrecision(any(), any()) } returns Unit
            coEvery { currencyService.fetchCurrency(any()) } returns currency
            coEvery { walletOwnerManager.findWalletOwner(USER_UUID) } returns owner
            coEvery {
                walletManager.findWalletByOwnerAndCurrencyAndType(
                    owner,
                    WalletType.MAIN,
                    currency
                )
            } returns createWallet(owner, WalletType.MAIN)
            coEvery {
                walletManager.findWalletByOwnerAndCurrencyAndType(
                    owner,
                    WalletType.CASHOUT,
                    currency
                )
            } returns createWallet(owner, WalletType.CASHOUT)
            coEvery { gatewayService.fetchGateway(any(), any()) } returns OnChainGatewayCommand(
                currencySymbol = CURRENCY,
                implementationSymbol = DEST_SYMBOL,
                chain = DEST_NETWORK,
                decimal = DECIMAL,
                isWithdrawActive = true,
                withdrawAllowed = true,
                withdrawFee = WITHDRAW_FEE,
                withdrawMin = MIN_AMOUNT,
                withdrawMax = MAX_AMOUNT
            )

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { withdrawService.requestWithdraw(command, TOKEN) }
            }

            Assertions.assertEquals(OpexError.WithdrawAmountLessThanMinimum, ex.error)
        }

        @Test
        fun `should throw WithdrawAmountGreaterThanMaximum when amount exceeds maximum`() = runBlocking {
            val command = createWithdrawCommand(amount = BigDecimal("100"))
            val owner = createOwner()
            val currency = createCurrency()

            coEvery { precisionService.validatePrecision(any(), any()) } returns Unit
            coEvery { currencyService.fetchCurrency(any()) } returns currency
            coEvery { walletOwnerManager.findWalletOwner(USER_UUID) } returns owner
            coEvery {
                walletManager.findWalletByOwnerAndCurrencyAndType(
                    owner,
                    WalletType.MAIN,
                    currency
                )
            } returns createWallet(owner, WalletType.MAIN, BigDecimal("1000"))
            coEvery {
                walletManager.findWalletByOwnerAndCurrencyAndType(
                    owner,
                    WalletType.CASHOUT,
                    currency
                )
            } returns createWallet(owner, WalletType.CASHOUT)
            coEvery { gatewayService.fetchGateway(any(), any()) } returns OnChainGatewayCommand(
                currencySymbol = CURRENCY,
                implementationSymbol = DEST_SYMBOL,
                chain = DEST_NETWORK,
                decimal = DECIMAL,
                isWithdrawActive = true,
                withdrawAllowed = true,
                withdrawFee = WITHDRAW_FEE,
                withdrawMin = MIN_AMOUNT,
                withdrawMax = MAX_AMOUNT
            )

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { withdrawService.requestWithdraw(command, TOKEN) }
            }

            Assertions.assertEquals(OpexError.WithdrawAmountGreaterThanMaximum, ex.error)
        }

        @Test
        fun `should create withdraw request successfully without OTP requirement`() = runBlocking {
            val command = createWithdrawCommand()
            val owner = createOwner()
            val currency = createCurrency()
            val withdraw = createWithdraw()

            coEvery { precisionService.validatePrecision(any(), any()) } returns Unit
            coEvery { currencyService.fetchCurrency(any()) } returns currency
            coEvery { walletOwnerManager.findWalletOwner(USER_UUID) } returns owner
            coEvery {
                walletManager.findWalletByOwnerAndCurrencyAndType(
                    owner,
                    WalletType.MAIN,
                    currency
                )
            } returns createWallet(owner, WalletType.MAIN)
            coEvery {
                walletManager.findWalletByOwnerAndCurrencyAndType(
                    owner,
                    WalletType.CASHOUT,
                    currency
                )
            } returns createWallet(owner, WalletType.CASHOUT)
            coEvery { gatewayService.fetchGateway(any(), any()) } returns OnChainGatewayCommand(
                currencySymbol = CURRENCY,
                implementationSymbol = DEST_SYMBOL,
                chain = DEST_NETWORK,
                decimal = DECIMAL,
                isWithdrawActive = true,
                withdrawAllowed = true,
                withdrawFee = WITHDRAW_FEE,
                withdrawMin = MIN_AMOUNT,
                withdrawMax = MAX_AMOUNT
            )
            coEvery { withdrawPersister.persist(any()) } returns withdraw
            coEvery { withdrawPersister.findById(any()) } returns withdraw
            coEvery { transferManager.transfer(any()) } returns TransferResultDetailed(mockk(), "tx-test")

            val result = withdrawService.requestWithdraw(command, TOKEN)

            Assertions.assertEquals(WITHDRAW_ID, result.withdrawId)
            Assertions.assertEquals(WithdrawStatus.CREATED, result.status)
            Assertions.assertEquals(WithdrawNextAction.WAITING_FOR_ADMIN, result.nextAction)
        }

        @Test
        fun `should create withdraw request with OTP requirement`() = runBlocking {
            withdrawService = WithdrawService(
                withdrawPersister, walletManager, walletOwnerManager, currencyService,
                transferManager, meterRegistry, gatewayService, precisionService,
                accountantProxy, withdrawRequestEventSubmitter, otpProxy, profileProxy,
                withdrawOtpPersister, gatewayPersister, SYSTEM_UUID,
                withdrawLimitEnabled = false, otpRequiredCount = 2
            )

            val command = createWithdrawCommand()
            val owner = createOwner()
            val currency = createCurrency()
            val withdraw = createWithdraw()

            coEvery { precisionService.validatePrecision(any(), any()) } returns Unit
            coEvery { currencyService.fetchCurrency(any()) } returns currency
            coEvery { walletOwnerManager.findWalletOwner(USER_UUID) } returns owner
            coEvery {
                walletManager.findWalletByOwnerAndCurrencyAndType(
                    owner,
                    WalletType.MAIN,
                    currency
                )
            } returns createWallet(owner, WalletType.MAIN)
            coEvery {
                walletManager.findWalletByOwnerAndCurrencyAndType(
                    owner,
                    WalletType.CASHOUT,
                    currency
                )
            } returns createWallet(owner, WalletType.CASHOUT)
            coEvery { gatewayService.fetchGateway(any(), any()) } returns OnChainGatewayCommand(
                currencySymbol = CURRENCY,
                implementationSymbol = DEST_SYMBOL,
                chain = DEST_NETWORK,
                decimal = DECIMAL,
                isWithdrawActive = true,
                withdrawAllowed = true,
                withdrawFee = WITHDRAW_FEE,
                withdrawMin = MIN_AMOUNT,
                withdrawMax = MAX_AMOUNT
            )
            coEvery { withdrawPersister.persist(any()) } returns withdraw

            val result = withdrawService.requestWithdraw(command, TOKEN)

            Assertions.assertEquals(WITHDRAW_ID, result.withdrawId)
            Assertions.assertEquals(WithdrawStatus.REQUESTED, result.status)
            Assertions.assertEquals(WithdrawNextAction.OTP_EMAIL, result.nextAction)
        }
    }

    @Nested
    inner class RequestOTPTests {

        @Test
        fun `should throw WithdrawNotFound when withdraw does not exist`() = runBlocking {
            coEvery { withdrawPersister.findById(WITHDRAW_ID) } returns null

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { withdrawService.requestOTP(TOKEN, USER_UUID, WITHDRAW_ID, OTPType.SMS) }
            }

            Assertions.assertEquals(OpexError.WithdrawNotFound, ex.error)
        }

        @Test
        fun `should throw Forbidden when user does not own withdraw`() = runBlocking {
            val withdraw = createWithdraw().copy(ownerUuid = "different-user")
            coEvery { withdrawPersister.findById(WITHDRAW_ID) } returns withdraw

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { withdrawService.requestOTP(TOKEN, USER_UUID, WITHDRAW_ID, OTPType.SMS) }
            }

            Assertions.assertEquals(OpexError.Forbidden, ex.error)
        }

        @Test
        fun `should throw OTPCannotBeRequested when withdraw status is not REQUESTED`() = runBlocking {
            val withdraw = createWithdraw(WithdrawStatus.ACCEPTED)
            coEvery { withdrawPersister.findById(WITHDRAW_ID) } returns withdraw

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { withdrawService.requestOTP(TOKEN, USER_UUID, WITHDRAW_ID, OTPType.SMS) }
            }

            Assertions.assertEquals(OpexError.OTPCannotBeRequested, ex.error)
        }

        @Test
        fun `should throw OTPAlreadyRequested when OTP already exists for type`() = runBlocking {
            val withdraw = createWithdraw()
            val existingOtp = WithdrawOtp(WITHDRAW_ID, "trace-123", OTPType.SMS, LocalDateTime.now())

            coEvery { withdrawPersister.findById(WITHDRAW_ID) } returns withdraw
            coEvery { withdrawOtpPersister.findByWithdrawId(WITHDRAW_ID) } returns listOf(existingOtp)

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { withdrawService.requestOTP(TOKEN, USER_UUID, WITHDRAW_ID, OTPType.SMS) }
            }

            Assertions.assertEquals(OpexError.OTPAlreadyRequested, ex.error)
        }

        @Test
        fun `should request SMS OTP successfully`() = runBlocking {
            val withdraw = createWithdraw()
            val profile = createProfile()
            val otpResponse = TempOtpResponse(VALID_OTP, listOf())

            coEvery { withdrawPersister.findById(WITHDRAW_ID) } returns withdraw
            coEvery { withdrawOtpPersister.findByWithdrawId(WITHDRAW_ID) } returns emptyList()
            coEvery { profileProxy.getProfile(TOKEN) } returns profile
            coEvery { otpProxy.requestOTP(any()) } returns otpResponse

            val result = withdrawService.requestOTP(TOKEN, USER_UUID, WITHDRAW_ID, OTPType.SMS)

            Assertions.assertEquals(VALID_OTP, result.otp)
            coVerify { otpProxy.requestOTP(match { it.userId == MOBILE }) }
        }

        @Test
        fun `should request EMAIL OTP successfully`() = runBlocking {
            val withdraw = createWithdraw()
            val profile = createProfile()
            val otpResponse = TempOtpResponse(VALID_OTP, listOf())

            coEvery { withdrawPersister.findById(WITHDRAW_ID) } returns withdraw
            coEvery { withdrawOtpPersister.findByWithdrawId(WITHDRAW_ID) } returns emptyList()
            coEvery { profileProxy.getProfile(TOKEN) } returns profile
            coEvery { otpProxy.requestOTP(any()) } returns otpResponse

            val result = withdrawService.requestOTP(TOKEN, USER_UUID, WITHDRAW_ID, OTPType.EMAIL)

            Assertions.assertEquals(VALID_OTP, result.otp)
            coVerify { otpProxy.requestOTP(match { it.userId == EMAIL }) }
        }
    }

    @Nested
    inner class VerifyOTPTests {

        @Test
        fun `should throw InvalidOTP when OTP verification fails`() = runBlocking {
            withdrawService = WithdrawService(
                withdrawPersister, walletManager, walletOwnerManager, currencyService,
                transferManager, meterRegistry, gatewayService, precisionService,
                accountantProxy, withdrawRequestEventSubmitter, otpProxy, profileProxy,
                withdrawOtpPersister, gatewayPersister, SYSTEM_UUID,
                withdrawLimitEnabled = false, otpRequiredCount = 2
            )

            val profile = createProfile()
            coEvery { profileProxy.getProfile(TOKEN) } returns profile
            coEvery { otpProxy.verifyOTP(any()) } returns OTPVerifyResponse(false, OTPResultType.INVALID, "trace-123")

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { withdrawService.verifyOTP(TOKEN, WITHDRAW_ID, OTPType.SMS, INVALID_OTP) }
            }

            Assertions.assertEquals(OpexError.InvalidOTP, ex.error)
        }

        @Test
        fun `should verify SMS OTP and return next action for EMAIL`() = runBlocking {
            withdrawService = WithdrawService(
                withdrawPersister, walletManager, walletOwnerManager, currencyService,
                transferManager, meterRegistry, gatewayService, precisionService,
                accountantProxy, withdrawRequestEventSubmitter, otpProxy, profileProxy,
                withdrawOtpPersister, gatewayPersister, SYSTEM_UUID,
                withdrawLimitEnabled = false, otpRequiredCount = 2
            )

            val profile = createProfile()
            coEvery { profileProxy.getProfile(TOKEN) } returns profile
            coEvery { otpProxy.verifyOTP(any()) } returns OTPVerifyResponse(true, OTPResultType.VALID, "trace-321")
            coEvery { withdrawOtpPersister.save(any()) } returns Unit
            coEvery { withdrawOtpPersister.findByWithdrawId(WITHDRAW_ID) } returns listOf(mockk())

            val result = withdrawService.verifyOTP(TOKEN, WITHDRAW_ID, OTPType.SMS, VALID_OTP)

            Assertions.assertEquals(WithdrawStatus.REQUESTED, result.status)
            Assertions.assertEquals(WithdrawNextAction.OTP_EMAIL, result.nextAction)
            coVerify { withdrawOtpPersister.save(match { it.otpType == OTPType.SMS }) }
        }

        @Test
        fun `should verify EMAIL OTP and return next action for SMS`() = runBlocking {
            withdrawService = WithdrawService(
                withdrawPersister, walletManager, walletOwnerManager, currencyService,
                transferManager, meterRegistry, gatewayService, precisionService,
                accountantProxy, withdrawRequestEventSubmitter, otpProxy, profileProxy,
                withdrawOtpPersister, gatewayPersister, SYSTEM_UUID,
                withdrawLimitEnabled = false, otpRequiredCount = 2
            )

            val profile = createProfile()
            coEvery { profileProxy.getProfile(TOKEN) } returns profile
            coEvery { otpProxy.verifyOTP(any()) } returns OTPVerifyResponse(true, OTPResultType.VALID, "trace-456")
            coEvery { withdrawOtpPersister.save(any()) } returns Unit
            coEvery { withdrawOtpPersister.findByWithdrawId(WITHDRAW_ID) } returns listOf(mockk())

            val result = withdrawService.verifyOTP(TOKEN, WITHDRAW_ID, OTPType.EMAIL, VALID_OTP)

            Assertions.assertEquals(WithdrawStatus.REQUESTED, result.status)
            Assertions.assertEquals(WithdrawNextAction.OTP_MOBILE, result.nextAction)
            coVerify { withdrawOtpPersister.save(match { it.otpType == OTPType.EMAIL }) }
        }

        @Test
        fun `should create withdraw when all OTPs verified`() = runBlocking {
            withdrawService = WithdrawService(
                withdrawPersister, walletManager, walletOwnerManager, currencyService,
                transferManager, meterRegistry, gatewayService, precisionService,
                accountantProxy, withdrawRequestEventSubmitter, otpProxy, profileProxy,
                withdrawOtpPersister, gatewayPersister, SYSTEM_UUID,
                withdrawLimitEnabled = false, otpRequiredCount = 2
            )

            val profile = createProfile()
            val withdraw = createWithdraw()
            val owner = createOwner()
            val currency = createCurrency()

            coEvery { profileProxy.getProfile(TOKEN) } returns profile
            coEvery { otpProxy.verifyOTP(any()) } returns OTPVerifyResponse(true, OTPResultType.VALID, "trace-789")
            coEvery { withdrawOtpPersister.save(any()) } returns Unit
            coEvery { withdrawOtpPersister.findByWithdrawId(WITHDRAW_ID) } returns listOf(mockk(), mockk())
            coEvery { withdrawPersister.findById(WITHDRAW_ID) } returns withdraw
            coEvery { currencyService.fetchCurrency(any()) } returns currency
            coEvery { walletOwnerManager.findWalletOwner(USER_UUID) } returns owner
            coEvery { walletManager.findWalletByOwnerAndCurrencyAndType(any(), any(), any()) } returns createWallet(
                owner,
                WalletType.MAIN
            )
            coEvery { transferManager.transfer(any()) } returns TransferResultDetailed(mockk(), "tx-test")
            coEvery { withdrawPersister.persist(any()) } returns withdraw

            val result = withdrawService.verifyOTP(TOKEN, WITHDRAW_ID, OTPType.SMS, VALID_OTP)

            Assertions.assertEquals(WithdrawStatus.CREATED, result.status)
            Assertions.assertEquals(WithdrawNextAction.WAITING_FOR_ADMIN, result.nextAction)
        }
    }

    @Nested
    inner class AcceptWithdrawTests {

        @Test
        fun `should throw WithdrawNotFound when withdraw does not exist`() = runBlocking {
            val command = WithdrawAcceptCommand(WITHDRAW_ID, BigDecimal.ONE, "tx-ref-123", "test", "test", "test")
            coEvery { withdrawPersister.findById(WITHDRAW_ID) } returns null

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { withdrawService.acceptWithdraw(command) }
            }

            Assertions.assertEquals(OpexError.WithdrawNotFound, ex.error)
        }

        @Test
        fun `should throw WithdrawCannotBeAccepted when status does not allow acceptance`() = runBlocking {
            val command = WithdrawAcceptCommand(WITHDRAW_ID, BigDecimal.ONE, "tx-ref-123", "test", "test", "test")
            val withdraw = createWithdraw(WithdrawStatus.REQUESTED)
            coEvery { withdrawPersister.findById(WITHDRAW_ID) } returns withdraw

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { withdrawService.acceptWithdraw(command) }
            }

            Assertions.assertEquals(OpexError.WithdrawCannotBeAccepted, ex.error)
        }

        @Test
        fun `should accept withdraw successfully`() = runBlocking {
            val command = WithdrawAcceptCommand(WITHDRAW_ID, BigDecimal.ONE, "tx-ref-123", "test", "test", "test")
            val withdraw = createWithdraw(WithdrawStatus.CREATED)
            val systemOwner = createSystemOwner()
            val currency = createCurrency()
            val sourceWallet = createWallet(createOwner(), WalletType.CASHOUT)
            val receiverWallet = createWallet(systemOwner, WalletType.MAIN)

            coEvery { walletOwnerManager.findWalletOwner(SYSTEM_UUID) } returns systemOwner
            coEvery { withdrawPersister.findById(WITHDRAW_ID) } returns withdraw
            coEvery { walletManager.findWalletById(WALLET_ID) } returns sourceWallet
            coEvery {
                walletManager.findWalletByOwnerAndCurrencyAndType(any(), any(), any())
            } returns receiverWallet
            coEvery { transferManager.transfer(any()) } returns TransferResultDetailed(mockk(), "tx-test")
            coEvery { withdrawPersister.persist(any()) } returns withdraw.copy(status = WithdrawStatus.ACCEPTED)

            val result = withdrawService.acceptWithdraw(command)

            Assertions.assertEquals(WITHDRAW_ID, result.withdrawId)
            Assertions.assertEquals(WithdrawStatus.ACCEPTED, result.status)
            coVerify { transferManager.transfer(any()) }
            coVerify { withdrawPersister.persist(match { it.status == WithdrawStatus.ACCEPTED }) }
        }

        @Test
        fun `should create system wallet if not exists when accepting withdraw`() = runBlocking {
            val command = WithdrawAcceptCommand(WITHDRAW_ID, BigDecimal.ONE, "tx-ref-123", "test", "test", "test")
            val withdraw = createWithdraw(WithdrawStatus.CREATED)
            val systemOwner = createSystemOwner()
            val currency = createCurrency()
            val sourceWallet = createWallet(createOwner(), WalletType.CASHOUT)
            val newWallet = createWallet(systemOwner, WalletType.MAIN)

            coEvery { walletOwnerManager.findWalletOwner(SYSTEM_UUID) } returns systemOwner
            coEvery { withdrawPersister.findById(WITHDRAW_ID) } returns withdraw
            coEvery { walletManager.findWalletById(WALLET_ID) } returns sourceWallet
            coEvery {
                walletManager.findWalletByOwnerAndCurrencyAndType(any(), any(), any())
            } returns null
            coEvery { walletManager.createWallet(any(), any(), any(), any()) } returns newWallet
            coEvery { transferManager.transfer(any()) } returns TransferResultDetailed(mockk(), "tx-test")
            coEvery { withdrawPersister.persist(any()) } returns withdraw.copy(status = WithdrawStatus.ACCEPTED)

            withdrawService.acceptWithdraw(command)

            coVerify {
                walletManager.createWallet(systemOwner, any(), match { it.symbol == currency.symbol }, WalletType.MAIN)
            }
        }
    }

    @Nested
    inner class CancelWithdrawTests {

        @Test
        fun `should throw WithdrawNotFound when withdraw does not exist`() = runBlocking {
            coEvery { withdrawPersister.findById(WITHDRAW_ID) } returns null

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { withdrawService.cancelWithdraw(USER_UUID, WITHDRAW_ID) }
            }

            Assertions.assertEquals(OpexError.WithdrawNotFound, ex.error)
        }

        @Test
        fun `should throw Forbidden when user does not own withdraw`() = runBlocking {
            val withdraw = createWithdraw().copy(ownerUuid = "different-user")
            coEvery { withdrawPersister.findById(WITHDRAW_ID) } returns withdraw

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { withdrawService.cancelWithdraw(USER_UUID, WITHDRAW_ID) }
            }

            Assertions.assertEquals(OpexError.Forbidden, ex.error)
        }

        @Test
        fun `should throw WithdrawCannotBeCanceled when status does not allow cancellation`() = runBlocking {
            val withdraw = createWithdraw(WithdrawStatus.ACCEPTED)
            coEvery { withdrawPersister.findById(WITHDRAW_ID) } returns withdraw

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { withdrawService.cancelWithdraw(USER_UUID, WITHDRAW_ID) }
            }

            Assertions.assertEquals(OpexError.WithdrawCannotBeCanceled, ex.error)
        }

        @Test
        fun `should cancel withdraw successfully`() = runBlocking {
            val withdraw = createWithdraw(WithdrawStatus.CREATED)
            val owner = createOwner()
            val currency = createCurrency()
            val sourceWallet = createWallet(owner, WalletType.CASHOUT)
            val receiverWallet = createWallet(owner, WalletType.MAIN)

            coEvery { withdrawPersister.findById(WITHDRAW_ID) } returns withdraw
            coEvery { currencyService.fetchCurrency(any()) } returns currency
            coEvery { walletOwnerManager.findWalletOwner(USER_UUID) } returns owner
            coEvery {
                walletManager.findWalletByOwnerAndCurrencyAndType(
                    owner,
                    WalletType.CASHOUT,
                    currency
                )
            } returns sourceWallet
            coEvery {
                walletManager.findWalletByOwnerAndCurrencyAndType(
                    owner,
                    WalletType.MAIN,
                    currency
                )
            } returns receiverWallet
            coEvery { withdrawPersister.persist(any()) } returns withdraw.copy(status = WithdrawStatus.CANCELED)
            coEvery { transferManager.transfer(any()) } returns TransferResultDetailed(mockk(), "tx-test")

            withdrawService.cancelWithdraw(USER_UUID, WITHDRAW_ID)

            coVerify { withdrawPersister.persist(match { it.status == WithdrawStatus.CANCELED }) }
            coVerify { transferManager.transfer(match { it.transferCategory == TransferCategory.WITHDRAW_CANCEL }) }
        }
    }

    @Nested
    inner class RejectWithdrawTests {

        @Test
        fun `should throw WithdrawNotFound when withdraw does not exist`() = runBlocking {
            val command = WithdrawRejectCommand(WITHDRAW_ID, "Invalid address", null, "applicator")
            coEvery { withdrawPersister.findById(WITHDRAW_ID) } returns null

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { withdrawService.rejectWithdraw(command) }
            }

            Assertions.assertEquals(OpexError.WithdrawNotFound, ex.error)
        }

        @Test
        fun `should throw WithdrawCannotBeRejected when status does not allow rejection`() = runBlocking {
            val command = WithdrawRejectCommand(WITHDRAW_ID, "Invalid address", null, "applicator")
            val withdraw = createWithdraw(WithdrawStatus.DONE)
            val owner = createOwner()
            val sourceWallet = createWallet(owner, WalletType.CASHOUT)
            val receiverWallet = createWallet(owner, WalletType.MAIN)

            coEvery { withdrawPersister.findById(WITHDRAW_ID) } returns withdraw
            coEvery { walletManager.findWalletById(WALLET_ID) } returns sourceWallet
            coEvery {
                walletManager.findWalletByOwnerAndCurrencyAndType(
                    owner,
                    WalletType.MAIN,
                    any()
                )
            } returns receiverWallet
            coEvery { transferManager.transfer(any()) } returns TransferResultDetailed(mockk(), "tx-test")
            coEvery { withdrawPersister.persist(any()) } returns withdraw.copy(status = WithdrawStatus.REJECTED)

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { withdrawService.rejectWithdraw(command) }
            }

            Assertions.assertEquals(OpexError.WithdrawCannotBeRejected, ex.error)
        }

        @Test
        fun `should reject withdraw successfully`() = runBlocking {
            val command = WithdrawRejectCommand(WITHDRAW_ID, "Invalid address", null, "applicator")
            val withdraw = createWithdraw(WithdrawStatus.CREATED)
            val owner = createOwner()
            val sourceWallet = createWallet(owner, WalletType.CASHOUT)
            val receiverWallet = createWallet(owner, WalletType.MAIN)

            coEvery { withdrawPersister.findById(WITHDRAW_ID) } returns withdraw
            coEvery { walletManager.findWalletById(WALLET_ID) } returns sourceWallet
            coEvery {
                walletManager.findWalletByOwnerAndCurrencyAndType(
                    owner,
                    WalletType.MAIN,
                    any()
                )
            } returns receiverWallet
            coEvery { transferManager.transfer(any()) } returns TransferResultDetailed(mockk(), "tx-test")
            coEvery { withdrawPersister.persist(any()) } returns withdraw.copy(status = WithdrawStatus.REJECTED)

            val result = withdrawService.rejectWithdraw(command)

            Assertions.assertEquals(WITHDRAW_ID, result.withdrawId)
            Assertions.assertEquals(WithdrawStatus.REJECTED, result.status)
            coVerify { withdrawPersister.persist(match { it.status == WithdrawStatus.REJECTED && it.statusReason == "Invalid address" }) }
        }

        @Test
        fun `should create main wallet if not exists when rejecting withdraw`() = runBlocking {
            val command = WithdrawRejectCommand(WITHDRAW_ID, "Insufficient funds", null, "applicator")
            val withdraw = createWithdraw(WithdrawStatus.CREATED)
            val owner = createOwner()
            val sourceWallet = createWallet(owner, WalletType.CASHOUT)
            val newWallet = createWallet(owner, WalletType.MAIN)

            coEvery { withdrawPersister.findById(WITHDRAW_ID) } returns withdraw
            coEvery { walletManager.findWalletById(WALLET_ID) } returns sourceWallet
            coEvery { walletManager.findWalletByOwnerAndCurrencyAndType(owner, WalletType.MAIN, any()) } returns null
            coEvery { walletManager.createWallet(any(), any(), any(), any()) } returns newWallet
            coEvery { transferManager.transfer(any()) } returns TransferResultDetailed(mockk(), "tx-test")
            coEvery { withdrawPersister.persist(any()) } returns withdraw.copy(status = WithdrawStatus.REJECTED)

            withdrawService.rejectWithdraw(command)

            coVerify { walletManager.createWallet(owner, any(), any(), WalletType.MAIN) }
        }
    }

    @Nested
    inner class DoneWithdrawTests {

        @Test
        fun `should throw WithdrawNotFound when withdraw does not exist`() = runBlocking {
            coEvery { withdrawPersister.findById(WITHDRAW_ID) } returns null

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { withdrawService.doneWithdraw(WITHDRAW_ID) }
            }

            Assertions.assertEquals(OpexError.WithdrawNotFound, ex.error)
        }

        @Test
        fun `should throw WithdrawCannotBeDone when status does not allow completion`() = runBlocking {
            val withdraw = createWithdraw(WithdrawStatus.REQUESTED)
            coEvery { withdrawPersister.findById(WITHDRAW_ID) } returns withdraw

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { withdrawService.doneWithdraw(WITHDRAW_ID) }
            }

            Assertions.assertEquals(OpexError.WithdrawCannotBeDone, ex.error)
        }

        @Test
        fun `should mark withdraw as done successfully`() = runBlocking {
            val withdraw = createWithdraw(WithdrawStatus.ACCEPTED)
            coEvery { withdrawPersister.findById(WITHDRAW_ID) } returns withdraw
            coEvery { withdrawPersister.persist(any()) } returns withdraw.copy(status = WithdrawStatus.DONE)

            val result = withdrawService.doneWithdraw(WITHDRAW_ID)

            Assertions.assertEquals(WITHDRAW_ID, result.withdrawId)
            Assertions.assertEquals(WithdrawStatus.DONE, result.status)
            coVerify { withdrawPersister.persist(match { it.status == WithdrawStatus.DONE }) }
        }
    }

    @Nested
    inner class FindWithdrawTests {

        @Test
        fun `should return null when withdraw not found`() = runBlocking {
            coEvery { withdrawPersister.findWithdrawResponseById(WITHDRAW_ID) } returns null

            val result = withdrawService.findWithdraw(WITHDRAW_ID)

            Assertions.assertNull(result)
        }

        @Test
        fun `should return withdraw response when found`() = runBlocking {
            val withdrawResponse = mockk<WithdrawResponse>()
            coEvery { withdrawPersister.findWithdrawResponseById(WITHDRAW_ID) } returns withdrawResponse

            val result = withdrawService.findWithdraw(WITHDRAW_ID)

            Assertions.assertEquals(withdrawResponse, result)
        }
    }

    @Nested
    inner class FindByCriteriaTests {

        @Test
        fun `should find withdraws by criteria`() = runBlocking {
            val withdrawList = listOf(mockk<WithdrawResponse>(), mockk())
            val statuses = listOf(WithdrawStatus.CREATED, WithdrawStatus.ACCEPTED)

            coEvery {
                withdrawPersister.findByCriteria(
                    USER_UUID, CURRENCY, null, null, statuses, null, null, true, 0, 10
                )
            } returns withdrawList

            val result = withdrawService.findByCriteria(
                USER_UUID, CURRENCY, null, null, statuses, null, null, true, 0, 10
            )

            Assertions.assertEquals(2, result.size)
            coVerify {
                withdrawPersister.findByCriteria(
                    USER_UUID,
                    CURRENCY,
                    null,
                    null,
                    statuses,
                    null,
                    null,
                    true,
                    0,
                    10
                )
            }
        }

        @Test
        fun `should find withdraws with all criteria parameters`() = runBlocking {
            val withdrawList = listOf(mockk<WithdrawResponse>())
            val startTime = LocalDateTime.now().minusDays(7)
            val endTime = LocalDateTime.now()
            val statuses = listOf(WithdrawStatus.DONE)

            coEvery {
                withdrawPersister.findByCriteria(
                    USER_UUID, CURRENCY, "tx-ref", DEST_ADDRESS, statuses, startTime, endTime, false, 10, 20
                )
            } returns withdrawList

            val result = withdrawService.findByCriteria(
                USER_UUID, CURRENCY, "tx-ref", DEST_ADDRESS, statuses, startTime, endTime, false, 10, 20
            )

            Assertions.assertEquals(1, result.size)
        }
    }

    @Nested
    inner class FindWithdrawHistoryTests {

        @Test
        fun `should find withdraw history for user`() = runBlocking {
            val withdrawList = listOf(mockk<WithdrawResponse>(), mockk(), mockk())

            coEvery {
                withdrawPersister.findWithdrawHistory(USER_UUID, CURRENCY, null, null, 10, 0, false)
            } returns withdrawList

            val result = withdrawService.findWithdrawHistory(USER_UUID, CURRENCY, null, null, 10, 0, false)

            Assertions.assertEquals(3, result.size)
            coVerify { withdrawPersister.findWithdrawHistory(USER_UUID, CURRENCY, null, null, 10, 0, false) }
        }

        @Test
        fun `should find withdraw history with time range`() = runBlocking {
            val startTime = LocalDateTime.now().minusMonths(1)
            val endTime = LocalDateTime.now()
            val withdrawList = listOf(mockk<WithdrawResponse>())

            coEvery {
                withdrawPersister.findWithdrawHistory(USER_UUID, null, startTime, endTime, 50, 5, true)
            } returns withdrawList

            val result = withdrawService.findWithdrawHistory(USER_UUID, null, startTime, endTime, 50, 5, true)

            Assertions.assertEquals(1, result.size)
        }
    }

    @Nested
    inner class FindWithdrawHistoryCountTests {

        @Test
        fun `should return withdraw history count`() = runBlocking {
            coEvery {
                withdrawPersister.findWithdrawHistoryCount(USER_UUID, CURRENCY, null, null)
            } returns 42L

            val result = withdrawService.findWithdrawHistoryCount(USER_UUID, CURRENCY, null, null)

            Assertions.assertEquals(42L, result)
        }

        @Test
        fun `should return withdraw history count with time range`() = runBlocking {
            val startTime = LocalDateTime.now().minusDays(30)
            val endTime = LocalDateTime.now()

            coEvery {
                withdrawPersister.findWithdrawHistoryCount(USER_UUID, null, startTime, endTime)
            } returns 15L

            val result = withdrawService.findWithdrawHistoryCount(USER_UUID, null, startTime, endTime)

            Assertions.assertEquals(15L, result)
        }
    }

    @Nested
    inner class GetWithdrawSummaryTests {

        @Test
        fun `should return withdraw summary`() = runBlocking {
            val summaryList = listOf(
                TransactionSummary(CURRENCY, BigDecimal.TEN),
                TransactionSummary("ETH", BigDecimal.ONE)
            )

            coEvery {
                withdrawPersister.getWithdrawSummary(USER_UUID, null, null, null)
            } returns summaryList

            val result = withdrawService.getWithdrawSummary(USER_UUID, null, null, null)

            Assertions.assertEquals(2, result.size)
            Assertions.assertEquals(CURRENCY, result[0].currency)
            Assertions.assertEquals(BigDecimal.TEN, result[0].amount)
        }

        @Test
        fun `should return withdraw summary with parameters`() = runBlocking {
            val startTime = LocalDateTime.now().minusMonths(3)
            val endTime = LocalDateTime.now()
            val summaryList = listOf(TransactionSummary(CURRENCY, BigDecimal.ONE))

            coEvery {
                withdrawPersister.getWithdrawSummary(USER_UUID, startTime, endTime, 10)
            } returns summaryList

            val result = withdrawService.getWithdrawSummary(USER_UUID, startTime, endTime, 10)

            Assertions.assertEquals(1, result.size)
        }
    }

    @Nested
    inner class WithdrawLimitTests {

        @Test
        fun `should check withdraw limit when enabled`() = runBlocking {
            withdrawService = WithdrawService(
                withdrawPersister, walletManager, walletOwnerManager, currencyService,
                transferManager, meterRegistry, gatewayService, precisionService,
                accountantProxy, withdrawRequestEventSubmitter, otpProxy, profileProxy,
                withdrawOtpPersister, gatewayPersister, SYSTEM_UUID,
                withdrawLimitEnabled = true, otpRequiredCount = 0
            )

            val command = createWithdrawCommand()
            val currency = createCurrency()

            coEvery { precisionService.validatePrecision(any(), any()) } returns Unit
            coEvery { currencyService.fetchCurrency(any()) } returns currency
            coEvery { accountantProxy.canRequestWithdraw(any(), any(), any(), any()) } returns false

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { withdrawService.requestWithdraw(command, TOKEN) }
            }

            Assertions.assertEquals(OpexError.WithdrawAmountExceeds, ex.error)
            coVerify { accountantProxy.canRequestWithdraw(USER_UUID, any(), CURRENCY, WITHDRAW_AMOUNT) }
        }

        @Test
        fun `should allow withdraw when limit check passes`() = runBlocking {
            withdrawService = WithdrawService(
                withdrawPersister, walletManager, walletOwnerManager, currencyService,
                transferManager, meterRegistry, gatewayService, precisionService,
                accountantProxy, withdrawRequestEventSubmitter, otpProxy, profileProxy,
                withdrawOtpPersister, gatewayPersister, SYSTEM_UUID,
                withdrawLimitEnabled = true, otpRequiredCount = 0
            )

            val command = createWithdrawCommand()
            val owner = createOwner()
            val currency = createCurrency()
            val withdraw = createWithdraw()

            coEvery { precisionService.validatePrecision(any(), any()) } returns Unit
            coEvery { currencyService.fetchCurrency(any()) } returns currency
            coEvery { accountantProxy.canRequestWithdraw(any(), any(), any(), any()) } returns true
            coEvery { walletOwnerManager.findWalletOwner(USER_UUID) } returns owner
            coEvery {
                walletManager.findWalletByOwnerAndCurrencyAndType(
                    owner,
                    WalletType.MAIN,
                    currency
                )
            } returns createWallet(owner, WalletType.MAIN)
            coEvery {
                walletManager.findWalletByOwnerAndCurrencyAndType(
                    owner,
                    WalletType.CASHOUT,
                    currency
                )
            } returns createWallet(owner, WalletType.CASHOUT)
            coEvery { gatewayService.fetchGateway(any(), any()) } returns OnChainGatewayCommand(
                currencySymbol = CURRENCY,
                implementationSymbol = DEST_SYMBOL,
                chain = DEST_NETWORK,
                decimal = DECIMAL,
                isWithdrawActive = true,
                withdrawAllowed = true,
                withdrawFee = WITHDRAW_FEE,
                withdrawMin = MIN_AMOUNT,
                withdrawMax = MAX_AMOUNT
            )
            coEvery { withdrawPersister.persist(any()) } returns withdraw
            coEvery { withdrawPersister.findById(any()) } returns withdraw
            coEvery { transferManager.transfer(any()) } returns TransferResultDetailed(mockk(), "tx-test")
            coEvery { withdrawRequestEventSubmitter.send(any(), any(), any(), any(), any(), any()) } returns Unit

            val result = withdrawService.requestWithdraw(command, TOKEN)

            Assertions.assertEquals(WITHDRAW_ID, result.withdrawId)
            coVerify { accountantProxy.canRequestWithdraw(USER_UUID, any(), CURRENCY, WITHDRAW_AMOUNT) }
        }
    }
}
