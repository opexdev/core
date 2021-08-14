package co.nilin.opex.bcgateway.core.service

import co.nilin.opex.bcgateway.core.model.AddressType
import co.nilin.opex.bcgateway.core.model.AssignedAddress
import co.nilin.opex.bcgateway.core.model.CachedAddress
import co.nilin.opex.bcgateway.core.model.Chain
import co.nilin.opex.bcgateway.core.model.Currency
import co.nilin.opex.bcgateway.core.model.CurrencyImplementation
import co.nilin.opex.bcgateway.core.model.CurrencyInfo
import co.nilin.opex.bcgateway.core.spi.AssignedAddressHandler
import co.nilin.opex.bcgateway.core.spi.CachedAddressHandler
import co.nilin.opex.bcgateway.core.spi.CurrencyLoader
import java.lang.RuntimeException
import java.math.BigDecimal
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class AssignAddressServiceImplUnitTest {
    @Mock
    lateinit var currencyLoader: CurrencyLoader

    @Mock
    lateinit var assignedAddressHandler: AssignedAddressHandler

    @Mock
    lateinit var cachedAddressHandler: CachedAddressHandler

    val assignAddressServiceImpl: AssignAddressServiceImpl

    val currency = Currency("ETH", "Ethereum")
    val ethAddressType = AddressType(1, "ETH", "+*", ".*")
    val ethMemoAddressType = AddressType(2, "ETH", "+*", "+*")
    val ethChain = Chain("ETH_MAINNET", arrayListOf(ethAddressType), emptyList())
    val bscChain = Chain("BSC_MAINNET", arrayListOf(ethAddressType, ethMemoAddressType), emptyList())


    init {
        MockitoAnnotations.openMocks(this)
        assignAddressServiceImpl = AssignAddressServiceImpl(
            currencyLoader, assignedAddressHandler, cachedAddressHandler
        )
        runBlocking {
            val eth =
                CurrencyImplementation(currency, ethChain, false, null, null, true, BigDecimal.ONE, BigDecimal.TEN)
            val wrappedEth = CurrencyImplementation(
                currency,
                bscChain,
                false,
                "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2",
                "WETH",
                true,
                BigDecimal.ONE,
                BigDecimal.ONE
            )

            Mockito.`when`(currencyLoader.fetchCurrencyInfo(currency.symbol))
                .thenReturn(CurrencyInfo(currency, listOf(eth, wrappedEth)))
        }


    }

    @Test
    fun givenCachedAddressAndUserWithNoAssignedAddress_whenAssignAddress_thenCachedAddressAssigned() {
        runBlocking {
            val user = UUID.randomUUID().toString()
            Mockito.`when`(assignedAddressHandler.fetchAssignedAddresses(user, listOf(ethAddressType, ethMemoAddressType))).thenReturn(
                emptyList()
            )
            Mockito.`when`(cachedAddressHandler.peekCachedAddress(ethAddressType)).thenReturn(
                CachedAddress("0x1", null, ethAddressType)
            )
            Mockito.`when`(cachedAddressHandler.peekCachedAddress(ethMemoAddressType)).thenReturn(
                CachedAddress("0x2", "Memo", ethMemoAddressType)
            )
            val assignedAddress = assignAddressServiceImpl.assignAddress(user, currency)
            Assertions.assertEquals(
                listOf(
                    AssignedAddress(
                        user,
                        "0x1",
                        null,
                        ethAddressType,
                        mutableListOf(ethChain, bscChain)
                    ),
                    AssignedAddress(
                        user,
                        "0x2",
                        "Memo",
                        ethMemoAddressType,
                        mutableListOf(bscChain)
                    )
                ), assignedAddress
            )
        }
    }

    @Test
    fun givenNoCachedAddressAndUserWithNoAssignedAddress_whenAssignAddress_thenExcpetion() {
        runBlocking {
            val user = UUID.randomUUID().toString()
            Mockito.`when`(assignedAddressHandler.fetchAssignedAddresses(user, listOf(ethAddressType, ethMemoAddressType))).thenReturn(
                emptyList()
            )
            Mockito.`when`(cachedAddressHandler.peekCachedAddress(ethAddressType)).thenReturn(null)

            Assertions.assertThrows(RuntimeException::class.java) {
                runBlocking {
                    assignAddressServiceImpl.assignAddress(user, currency)
                }
            }
        }
    }

    @Test
    fun givenCachedAddressAndUserOneAssignedAddress_whenAssignAddress_thenCachedAddressAssigned() {
        runBlocking {
            val user = UUID.randomUUID().toString()
            Mockito.`when`(assignedAddressHandler.fetchAssignedAddresses(user, listOf(ethAddressType, ethMemoAddressType))).thenReturn(
                mutableListOf(
                    AssignedAddress( user,
                        "0x1",
                        null,
                        ethAddressType,
                        mutableListOf(ethChain)
                    )
                )
            )
            Mockito.`when`(cachedAddressHandler.peekCachedAddress(ethAddressType)).thenReturn(
                CachedAddress("0x1", null, ethAddressType)
            )
            Mockito.`when`(cachedAddressHandler.peekCachedAddress(ethMemoAddressType)).thenReturn(
                CachedAddress("0x2", "Memo", ethMemoAddressType)
            )
            val assignedAddress = assignAddressServiceImpl.assignAddress(user, currency)
            Assertions.assertEquals(
                listOf(
                    AssignedAddress(
                        user,
                        "0x1",
                        null,
                        ethAddressType,
                        mutableListOf(ethChain, bscChain)
                    ),
                    AssignedAddress(
                        user,
                        "0x2",
                        "Memo",
                        ethMemoAddressType,
                        mutableListOf(bscChain)
                    )
                ), assignedAddress
            )
        }
    }

}