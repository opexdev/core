package co.nilin.opex.auth.gateway.authenticator.service

import co.nilin.opex.auth.core.spi.WhiteListPersister
import kotlinx.coroutines.flow.filter
import org.springframework.stereotype.Service

@Service
class WhitelistService(private var whiteListManagement: WhiteListPersister) {
    fun userIsAllowed(userId:String):Boolean{
        whiteListManagement.getAll()?.filter { it==userId }?.let { return true }?: return false
    }
}