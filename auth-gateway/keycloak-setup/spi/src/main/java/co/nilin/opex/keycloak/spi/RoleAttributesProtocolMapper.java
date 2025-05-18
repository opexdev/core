package co.nilin.opex.keycloak.spi;

import org.keycloak.models.*;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class RoleAttributesProtocolMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper {

    private static final String PROVIDER_ID = "role-attributes-mapper";
    private static final String ROLE_ATTRIBUTES_CLAIM = "role_attributes";

    public static final String ATTRIBUTE_NAME = "attribute.name";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayCategory() {
        return "Role Attributes";
    }

    @Override
    public String getDisplayType() {
        return "Role Attributes Mapper";
    }

    @Override
    public String getHelpText() {
        return "Adds attributes of user's roles to the token.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        List<ProviderConfigProperty> configProperties = new ArrayList<>();
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);
        OIDCAttributeMapperHelper.addJsonTypeConfig(configProperties);

        ProviderConfigProperty attributeName = new ProviderConfigProperty();
        attributeName.setName(ATTRIBUTE_NAME);
        attributeName.setLabel("Attribute Name");
        attributeName.setType(ProviderConfigProperty.STRING_TYPE);
        attributeName.setHelpText("The name of the role attribute to include in the token.");
        configProperties.add(attributeName);
        return configProperties;
    }

    @Override
    public AccessToken transformAccessToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession session, UserSessionModel user, ClientSessionContext clientSessionCtx) {
        String attributeNameToInclude = mappingModel.getConfig().get(ATTRIBUTE_NAME);
        String claimName = mappingModel.getConfig().get(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME);
        var finalList = new ArrayList<>();

        List<RoleModel> userRoles = user.getUser().getRealmRoleMappingsStream().collect(Collectors.toList());
        for (RoleModel role : userRoles) {
            extract(role, attributeNameToInclude, finalList);
            var compositeRoles = role.getCompositesStream().collect(Collectors.toList());
            compositeRoles.forEach(r -> extract(r, attributeNameToInclude, finalList));
        }

        if (!finalList.isEmpty()) {
            token.getOtherClaims().put(claimName != null && !claimName.isEmpty() ? claimName : ROLE_ATTRIBUTES_CLAIM, finalList);
        }
        return token;
    }

    private void extract(RoleModel role, String attributeNameToInclude, ArrayList<Object> list) {
        var attributes = role.getAttributes();
        if (attributes.containsKey(attributeNameToInclude)) {
            var att = attributes.get(attributeNameToInclude);
            if (att.isEmpty())
                return;

            var value = att.get(0);
            var splt = value.split(",");
            for (var v : splt) {
                if (!v.isBlank())
                    list.add(v);
            }
        }
    }

}
