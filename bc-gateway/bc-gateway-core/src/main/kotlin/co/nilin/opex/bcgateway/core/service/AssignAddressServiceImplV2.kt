//package co.nilin.opex.bcgateway.core.service
//
//import co.nilin.opex.bcgateway.core.api.AssignAddressService
//import co.nilin.opex.bcgateway.core.model.*
//import co.nilin.opex.bcgateway.core.spi.*
//import co.nilin.opex.bcgateway.core.utils.LoggerDelegate
//import co.nilin.opex.common.OpexError
//import org.slf4j.Logger
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.transaction.annotation.Transactional
//import java.time.LocalDateTime
//import java.time.ZoneId
//
//open class AssignAddressServiceImplV2(
//        private val currencyHandler: CryptoCurrencyHandlerV2,
//        private val assignedAddressHandler: AssignedAddressHandler,
//        private val reservedAddressHandler: ReservedAddressHandler,
//        private val chainLoader: ChainLoader
//) : AssignAddressService {
//    @Value("\${app.address.life-time}")
//    private var lifeTime: Long? = null
//    private val logger: Logger by LoggerDelegate()
//
//    override suspend fun assignAddress(user: String, currencyImplUuid: String): List<AssignedAddressV2> {
//        logger.info(ZoneId.systemDefault().toString())
//        val result = mutableSetOf<AssignedAddressV2>()
//        currencyHandler.fetchCurrencyImpls(FetchImpls(currencyImplUuid))
//                ?.imps?.firstOrNull()?.let { it ->
//                    //for requested chain check all available address types and for each of them do :
//                    chainLoader.fetchChainInfo(it.chain!!).addressTypes.map { it -> it.id }.distinct()
//                            .forEach { addressType ->
//                                //check: Is there any assigned address(user, specific address type on requested chain)
//                                assignedAddressHandler.fetchAssignedAddresses(user, addressType)?.let {
//                                    result.add(it)
//                                } ?: run {
//                                    // there is no assigned address(user,specific address type on requested chain)
//                                    //then assign new address (ip applicable)
//                                    assignNewAddress(user, addressType)?.let { ra ->
//                                        result.add(ra)
//                                    }
//                                }
//
//
//                            }
//                    if (result.size == 0)
//                        throw OpexError.ReservedAddressNotAvailable.exception()
//                    return result.toMutableList()
//                } ?: throw OpexError.BadRequest.exception()
//
//    }
//
//    @Transactional
//    open suspend fun assignNewAddress(user: String, addressTypeId: Long): AssignedAddressV2? {
//        reservedAddressHandler.peekReservedAddress(addressTypeId)?.let {//
//            reservedAddressHandler.remove(it)
//            return assignedAddressHandler.persist(user,
//                    AssignedAddressV2(addressTypeId, it.address, it.memo,
//                            lifeTime?.let { LocalDateTime.now().plusSeconds(lifeTime!!) } ?: null,
//                            LocalDateTime.now(),
//                            null,
//                            AddressStatus.Assigned,
//                            null))
//        }
//
//                ?: run { return null }
//
//    }
//
//}
