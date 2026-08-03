import co.nilin.opex.otp.app.data.SMSProviderType
import co.nilin.opex.otp.app.model.SMSProviderRoute
import co.nilin.opex.otp.app.proxy.SMSProvider
import co.nilin.opex.otp.app.repository.SMSProviderRouteRepository
import co.nilin.opex.otp.app.service.SMSProviderRouter
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import reactor.core.publisher.Flux

class SMSProviderRouterTest {

    private val repository: SMSProviderRouteRepository = mock()

    private val twilioProvider: SMSProvider = mock()
    private val kavenegarProvider: SMSProvider = mock()
    private val smsToProvider: SMSProvider = mock()

    private lateinit var router: SMSProviderRouter

    @BeforeEach
    fun setUp() {
        Mockito.`when`(twilioProvider.type).thenReturn(SMSProviderType.TWILIO)
        Mockito.`when`(kavenegarProvider.type).thenReturn(SMSProviderType.KAVENEGAR)
        Mockito.`when`(smsToProvider.type).thenReturn(SMSProviderType.SMSTO)

        router = SMSProviderRouter(
            listOf(
                twilioProvider,
                kavenegarProvider,
                smsToProvider
            ),
            repository,
            SMSProviderType.KAVENEGAR
        )
    }

    @Test
    fun givenNoMatchingRoute_whenGetProvider_thenDefaultProviderReturned(): Unit = runBlocking {
        Mockito.`when`(repository.findAllByEnabledTrue())
            .thenReturn(
                Flux.just(
                    SMSProviderRoute(1, "+98", SMSProviderType.SMSTO.name),
                    SMSProviderRoute(2, "+989", SMSProviderType.KAVENEGAR.name),
                )
            )
        val provider = router.getProvider("+447700123456")
        assertEquals(kavenegarProvider, provider)
    }

    @Test
    fun givenNoMatchingRoute_whenGetProvider_thenLongestMatchedProviderReturned(): Unit = runBlocking {
        Mockito.`when`(repository.findAllByEnabledTrue())
            .thenReturn(
                Flux.just(
                    SMSProviderRoute(1, "+98", SMSProviderType.SMSTO.name),
                    SMSProviderRoute(2, "+989", SMSProviderType.KAVENEGAR.name),
                )
            )
        val provider = router.getProvider("+989556677788")
        assertEquals(kavenegarProvider, provider)
    }

    @Test
    fun givenNoMatchingRoute_whenGetProvider_thenMatchedProviderReturned(): Unit = runBlocking {
        Mockito.`when`(repository.findAllByEnabledTrue())
            .thenReturn(
                Flux.just(
                    SMSProviderRoute(1, "+98", SMSProviderType.SMSTO.name),
                    SMSProviderRoute(2, "+989", SMSProviderType.KAVENEGAR.name),
                    SMSProviderRoute(3, "+44", SMSProviderType.TWILIO.name),

                    )
            )
        val provider = router.getProvider("+44555777999")
        assertEquals(twilioProvider, provider)
    }
}