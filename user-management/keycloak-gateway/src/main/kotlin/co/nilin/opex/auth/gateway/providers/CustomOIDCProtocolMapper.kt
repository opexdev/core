package co.nilin.opex.auth.gateway.providers

import co.nilin.opex.auth.gateway.model.WhiteListModel
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import org.keycloak.connections.jpa.JpaConnectionProvider
import org.keycloak.models.ClientSessionContext
import org.keycloak.models.KeycloakSession
import org.keycloak.models.ProtocolMapperModel
import org.keycloak.models.UserSessionModel
import org.keycloak.protocol.oidc.OIDCLoginProtocol
import org.keycloak.protocol.oidc.mappers.*
import org.keycloak.provider.ProviderConfigProperty
import org.keycloak.representations.AccessToken
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import java.util.stream.Collectors
import javax.persistence.EntityManager


class CustomOIDCProtocolMapper() : AbstractOIDCProtocolMapper(), OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {
    private val logger = LoggerFactory.getLogger(CustomOIDCProtocolMapper::class.java)

    private val PROVIDER_ID = "oidc-customprotocolmapper"
    private val configProperties: List<ProviderConfigProperty> = ArrayList()

    @Value("\${app.whitelist.login.enable}")
    private var loginWhitelistIsEnable: Boolean? = true


    override fun getConfigProperties(): List<ProviderConfigProperty>? {
        return configProperties
    }

    override fun getDisplayCategory(): String? {
        return TOKEN_MAPPER_CATEGORY
    }

    override fun getDisplayType(): String? {
        return "Custom Claim Mapper"
    }

    override fun getId(): String? {
        return PROVIDER_ID
    }

    override fun getHelpText(): String? {
        return "some help text"
    }

    override fun transformAccessToken(token: AccessToken, mappingModel: ProtocolMapperModel?, keycloakSession: KeycloakSession?,
                                      userSession: UserSessionModel?, clientSessionCtx: ClientSessionContext?): AccessToken? {

        token.otherClaims["kyc_level"] = userSession?.user?.attributes?.get("kycLevel")
        setClaim(token, mappingModel, userSession, keycloakSession, clientSessionCtx)
        if (loginWhitelistIsEnable == true) {
            logger.info("login whitelist is enable, going to filter login requests ........")
            val em: EntityManager = keycloakSession!!.getProvider(JpaConnectionProvider::class.java).entityManager
            val result: List<WhiteListModel> = em.createQuery("from whitelist", WhiteListModel::class.java).resultList
            if (!result.stream()
                            .map(WhiteListModel::identifier)
                            .collect(Collectors.toList()).contains(userSession?.user?.email))
                throw OpexException(OpexError.LoginIsLimited)
        }
        return token

    }

    fun create(name: String?,
               accessToken: Boolean, idToken: Boolean, userInfo: Boolean): ProtocolMapperModel? {
        val mapper = ProtocolMapperModel()
        mapper.name = name
        mapper.protocolMapper = PROVIDER_ID
        mapper.protocol = OIDCLoginProtocol.LOGIN_PROTOCOL
        val config: MutableMap<String, String> = HashMap()
        config[OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN] = "true"
        config[OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN] = "true"
        mapper.config = config
        return mapper
    }
}