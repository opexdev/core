package co.nilin.opex.auth.gateway.providers

import org.keycloak.models.ClientSessionContext
import org.keycloak.models.KeycloakSession
import org.keycloak.models.ProtocolMapperModel
import org.keycloak.models.UserSessionModel
import org.keycloak.protocol.oidc.OIDCLoginProtocol
import org.keycloak.protocol.oidc.mappers.*
import org.keycloak.provider.ProviderConfigProperty
import org.keycloak.representations.AccessToken
import org.slf4j.LoggerFactory


class CustomOIDCProtocolMapper() : AbstractOIDCProtocolMapper(), OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {
    private val logger = LoggerFactory.getLogger(CustomOIDCProtocolMapper::class.java)

    private val PROVIDER_ID = "oidc-customprotocolmapper"
    private val configProperties: List<ProviderConfigProperty> = ArrayList()
//    private val opexRealm = session.realms().getRealm("opex")

    /**
     * Maybe you want to have config fields for your Mapper
     */
    /*
    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(ProtocolMapperUtils.USER_ATTRIBUTE);
        property.setLabel(ProtocolMapperUtils.USER_MODEL_ATTRIBUTE_LABEL);
        property.setHelpText(ProtocolMapperUtils.USER_MODEL_ATTRIBUTE_HELP_TEXT);
        property.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(property);
property = new ProviderConfigProperty();
        property.setName(ProtocolMapperUtils.MULTIVALUED);
        property.setLabel(ProtocolMapperUtils.MULTIVALUED_LABEL);
        property.setHelpText(ProtocolMapperUtils.MULTIVALUED_HELP_TEXT);
        property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        configProperties.add(property);
    }
     */
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
        userI
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