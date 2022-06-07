package co.nilin.opex.bcgateway.core.service

import co.nilin.opex.bcgateway.core.model.*
import co.nilin.opex.bcgateway.core.model.Currency
import co.nilin.opex.bcgateway.core.spi.AssignedAddressHandler
import co.nilin.opex.bcgateway.core.spi.CurrencyHandler
import co.nilin.opex.bcgateway.core.spi.ReservedAddressHandler
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.math.BigDecimal
import java.util.*

class AssignAddressServiceImplUnitTest {
    @Mock
    lateinit var currencyHandler: CurrencyHandler

    @Mock
    lateinit var assignedAddressHandler: AssignedAddressHandler

    @Mock
    lateinit var reservedAddressHandler: ReservedAddressHandler

    val assignAddressServiceImpl: AssignAddressServiceImpl

    val currency = Currency("ETH", "Ethereum")
    val ethAddressType = AddressType(1, "ETH", "+*", ".*")
    val ethMemoAddressType = AddressType(2, "ETH", "+*", "+*")
    val ethChain = Chain("ETH_MAINNET", arrayListOf(ethAddressType))
    val bscChain = Chain("BSC_MAINNET", arrayListOf(ethAddressType, ethMemoAddressType))


    init {
        MockitoAnnotations.openMocks(this)
        assignAddressServiceImpl = AssignAddressServiceImpl(
            currencyHandler, assignedAddressHandler, reservedAddressHandler
        )
        runBlocking {
            val eth =
                CurrencyImplementation(currency, ethChain, false, null, null, true, BigDecimal.ONE, BigDecimal.TEN, 18)
            val wrappedEth = CurrencyImplementation(
                currency,
                bscChain,
                false,
                "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2",
                "WETH",
                true,
                BigDecimal.ONE,
                BigDecimal.ONE,
                18
            )

            Mockito.`when`(currencyHandler.fetchCurrencyInfo(currency.symbol))
                .thenReturn(CurrencyInfo(currency, listOf(eth, wrappedEth)))
        }


    }

    @Test
    fun givenReservedAddressAndUserWithNoAssignedAddress_whenAssignAddress_thenReservedAddressAssigned() {
        runBlocking {
            val user = UUID.randomUUID().toString()
            Mockito.`when`(
                assignedAddressHandler.fetchAssignedAddresses(
                    user,
                    listOf(ethAddressType, ethMemoAddressType)
                )
            ).thenReturn(
                emptyList()
            )
            Mockito.`when`(reservedAddressHandler.peekReservedAddress(ethAddressType)).thenReturn(
                ReservedAddress("0x1", null, ethAddressType)
            )
            Mockito.`when`(reservedAddressHandler.peekReservedAddress(ethMemoAddressType)).thenReturn(
                ReservedAddress("0x2", "Memo", ethMemoAddressType)
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
    fun givenNoReservedAddressAndUserWithNoAssignedAddress_whenAssignAddress_thenExcpetion() {
        runBlocking {
            val user = UUID.randomUUID().toString()
            Mockito.`when`(
                assignedAddressHandler.fetchAssignedAddresses(
                    user,
                    listOf(ethAddressType, ethMemoAddressType)
                )
            ).thenReturn(
                emptyList()
            )
            Mockito.`when`(reservedAddressHandler.peekReservedAddress(ethAddressType)).thenReturn(null)

            Assertions.assertThrows(RuntimeException::class.java) {
                runBlocking {
                    assignAddressServiceImpl.assignAddress(user, currency)
                }
            }
        }
    }

    @Test
    fun givenReservedAddressAndUserOneAssignedAddress_whenAssignAddress_thenReservedAddressAssigned() {
        runBlocking {
            val user = UUID.randomUUID().toString()
            Mockito.`when`(
                assignedAddressHandler.fetchAssignedAddresses(
                    user,
                    listOf(ethAddressType, ethMemoAddressType)
                )
            ).thenReturn(
                mutableListOf(
                    AssignedAddress(
                        user,
                        "0x1",
                        null,
                        ethAddressType,
                        mutableListOf(ethChain)
                    )
                )
            )
            Mockito.`when`(reservedAddressHandler.peekReservedAddress(ethAddressType)).thenReturn(
                ReservedAddress("0x1", null, ethAddressType)
            )
            Mockito.`when`(reservedAddressHandler.peekReservedAddress(ethMemoAddressType)).thenReturn(
                ReservedAddress("0x2", "Memo", ethMemoAddressType)
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
