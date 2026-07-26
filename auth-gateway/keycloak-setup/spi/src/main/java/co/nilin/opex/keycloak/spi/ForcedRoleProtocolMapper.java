package co.nilin.opex.keycloak.spi;

import org.keycloak.models.*;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ForcedRoleProtocolMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper {

    private static final String PROVIDER_ID = "forced-role-mapper";
    private static final String ROLE_ATTRIBUTES_CLAIM = "forced_role";

    public static final String KEY_NAME = "key.name";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayCategory() {
        return "Forced Role";
    }

    @Override
    public String getDisplayType() {
        return "Forced Role Mapper";
    }

    @Override
    public String getHelpText() {
        return "Forces the addition of roles to the token.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        List<ProviderConfigProperty> configProperties = new ArrayList<>();
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);
        OIDCAttributeMapperHelper.addJsonTypeConfig(configProperties);

        ProviderConfigProperty attributeName = new ProviderConfigProperty();
        attributeName.setName(KEY_NAME);
        attributeName.setLabel("Key Name");
        attributeName.setType(ProviderConfigProperty.STRING_TYPE);
        attributeName.setHelpText("The name of the role to include in the token.");
        configProperties.add(attributeName);
        return configProperties;
    }

    @Override
    public AccessToken transformAccessToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession session, UserSessionModel user, ClientSessionContext clientSessionCtx) {
        String claimName = mappingModel.getConfig().get(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME);
        var finalList = new HashSet<>();

        user.getUser().getRealmRoleMappingsStream().forEach(role -> {
            finalList.add(role.getName());
            role.getCompositesStream().forEach(r -> finalList.add(r.getName()));
        });

        if (!finalList.isEmpty()) {
            token.getOtherClaims().put(claimName != null && !claimName.isEmpty() ? claimName : ROLE_ATTRIBUTES_CLAIM, finalList);
        }

        return token;
    }

}
