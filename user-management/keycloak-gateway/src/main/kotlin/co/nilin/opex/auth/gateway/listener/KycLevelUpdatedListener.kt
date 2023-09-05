package co.nilin.opex.auth.gateway.listener

import co.nilin.opex.auth.core.data.KycLevelUpdatedEvent
import co.nilin.opex.auth.core.spi.KycLevelUpdatedEventListener
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.keycloak.models.KeycloakSession
import org.keycloak.models.KeycloakSessionFactory
import org.keycloak.models.RealmModel
import org.keycloak.representations.idm.UserRepresentation
import org.keycloak.services.resource.RealmResourceProvider
import org.keycloak.services.resources.KeycloakApplication

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


@Component
class KycLevelUpdatedListener() : KycLevelUpdatedEventListener {


    private val logger = LoggerFactory.getLogger(KycLevelUpdatedListener::class.java)
    val scope = CoroutineScope(Dispatchers.IO)
    var kcSession: KeycloakSession? = null
    var realm: RealmModel? = null

    override fun id(): String {
        return "KycLevelUpdatedListener"
    }

    override fun onEvent(event: KycLevelUpdatedEvent,
                         partition: Int, offset: Long, timestamp: Long, eventId: String) {
        val factory: KeycloakSessionFactory = KeycloakApplication.getSessionFactory()
        this.kcSession = factory.create()
        kcSession!!.transactionManager.begin()
        this.realm = kcSession!!.realms().getRealm("opex")

        logger.info("==========================================================================")
        logger.info("Incoming UserLevelUpdated event: $event")
        logger.info("==========================================================================")
        with(event) {
            val user = kcSession!!.users().getUserById(userId, realm) ?: throw OpexException(OpexError.UserNotFound)
            user.setSingleAttribute("kycLevel", kycLevel.name)
        }

        kcSession!!.transactionManager.commit()

    }


}