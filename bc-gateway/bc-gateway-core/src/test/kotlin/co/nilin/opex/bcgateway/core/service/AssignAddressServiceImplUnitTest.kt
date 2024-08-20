package co.nilin.opex.bcgateway.core.service

import co.nilin.opex.bcgateway.core.model.*
import co.nilin.opex.bcgateway.core.spi.AssignedAddressHandler
import co.nilin.opex.bcgateway.core.spi.ChainLoader
import co.nilin.opex.bcgateway.core.spi.CryptoCurrencyHandlerV2
//import co.nilin.opex.bcgateway.core.spi.CurrencyHandler
import co.nilin.opex.bcgateway.core.spi.ReservedAddressHandler
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import java.math.BigDecimal
import java.util.*

class AssignAddressServiceImplUnitTest {

    private val currencyHandler = mockk<CryptoCurrencyHandlerV2>()
    private val assignedAddressHandler = mockk<AssignedAddressHandler>()
    private val reservedAddressHandler = mockk<ReservedAddressHandler>()
    private val chainLoader = mockk<ChainLoader>()


    private val assignAddressServiceImpl =
            AssignAddressServiceImpl(currencyHandler, assignedAddressHandler, reservedAddressHandler, chainLoader)

    //    private val currency = Currency("ETH", "Ethereum")
    private val chain = "ETH_MAINNET"
    private val currency = "ETH"
    private val ethAddressType = AddressType(1, "ETH", "+*", ".*")
    private val ethMemoAddressType = AddressType(2, "ETH", "+*", "+*")
    private val ethChain = Chain("ETH_MAINNET", arrayListOf(ethAddressType))
    private val bscChain = Chain("BSC_MAINNET", arrayListOf(ethAddressType, ethMemoAddressType))


    init {
        val eth = CryptoCurrencyCommand(
                currency,
                UUID.randomUUID().toString(),
                currency,
                true,
                false,
                null,
                null,
                BigDecimal.ZERO,
                true,
                true,
                BigDecimal.ZERO,
                18,
                ethChain.name,
//                ethChain
        )


        val wrappedEth = CryptoCurrencyCommand(
                currency,
                UUID.randomUUID().toString(),
                currency,
                true,
                false,
                "WETH",
                "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2",
                BigDecimal.ZERO,
                true,
                true,
                BigDecimal.ZERO,
                18,
                bscChain.name,
//                bscChain

        )

        coEvery { currencyHandler.fetchCurrencyOnChainGateways(FetchGateways(currencySymbol = currency)) } returns CurrencyImps(
                listOf(eth, wrappedEth)
        )
        coEvery { chainLoader.fetchChainInfo(chain = ethChain.name) } returns ethChain

        coEvery { chainLoader.fetchChainInfo(chain = bscChain.name) } returns bscChain


        coEvery { assignedAddressHandler.persist(any()) } returns Unit
        coEvery { reservedAddressHandler.remove(any()) } returns Unit
    }

    @Test
    fun givenReservedAddressAndUserWithNoAssignedAddress_whenAssignAddress_thenReservedAddressAssigned(): Unit =
            runBlocking {
                val user = UUID.randomUUID().toString()
                coEvery {
                    assignedAddressHandler.fetchAssignedAddresses(
                            eq(user),
                            any()
                    )
                } returns emptyList()

                coEvery { reservedAddressHandler.peekReservedAddress(ethAddressType) } returns ReservedAddress(
                        "0x1",
                        null,
                        ethAddressType
                )

                coEvery { reservedAddressHandler.peekReservedAddress(ethMemoAddressType) } returns ReservedAddress(
                        "0x2",
                        "Memo",
                        ethMemoAddressType
                )

                val assignedAddress = assignAddressServiceImpl.assignAddress(user, currency, chain)
                assertThat(assignedAddress).isEqualTo(
                        listOf(
                                AssignedAddress(
                                        user,
                                        "0x1",
                                        null,
                                        ethAddressType,
                                        mutableListOf(ethChain)

                                )
                        )
                )
            }

    @Test
    fun givenNoReservedAddressAndUserWithNoAssignedAddress_whenAssignAddress_thenExcpetion(): Unit = runBlocking {
        val user = UUID.randomUUID().toString()
        coEvery {
            assignedAddressHandler.fetchAssignedAddresses(
                    user,
                    listOf(ethAddressType, ethMemoAddressType)
            )
        } returns emptyList()
        coEvery { reservedAddressHandler.peekReservedAddress(ethAddressType) } returns null
        coEvery { assignAddressServiceImpl.assignAddress(user, currency, chain) } throws RuntimeException()
    }

    @Test
    fun givenReservedAddressAndUserOneAssignedAddress_whenAssignAddress_thenReservedAddressAssigned(): Unit =
            runBlocking {
                val user = UUID.randomUUID().toString()
                coEvery {
                    assignedAddressHandler.fetchAssignedAddresses(
                            eq(user),
                            any()
                    )
                } returns mutableListOf(
                        AssignedAddress(
                                user,
                                "0x1",
                                null,
                                ethAddressType,
                                mutableListOf(ethChain)
                        )
                )
                coEvery { reservedAddressHandler.peekReservedAddress(ethAddressType) } returns ReservedAddress(
                        "0x1",
                        null,
                        ethAddressType
                )
                coEvery { reservedAddressHandler.peekReservedAddress(ethMemoAddressType) } returns ReservedAddress(
                        "0x2",
                        "Memo",
                        ethMemoAddressType
                )

                val assignedAddress = assignAddressServiceImpl.assignAddress(user, currency, chain)
                assertThat(assignedAddress).isEqualTo(
                        listOf(
                                AssignedAddress(
                                        user,
                                        "0x1",
                                        null,
                                        ethAddressType,
                                        mutableListOf(ethChain)
                                )
                        )
                )
            }

}
