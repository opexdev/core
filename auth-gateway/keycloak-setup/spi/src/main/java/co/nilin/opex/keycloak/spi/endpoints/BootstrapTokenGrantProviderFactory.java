package co.nilin.opex.keycloak.spi.endpoints;

import co.nilin.opex.keycloak.spi.BootstrapTokenGrantAuthenticator;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import java.util.Collections;
import java.util.List;

public class BootstrapTokenGrantProviderFactory implements AuthenticatorFactory {

    // 1. Change PROVIDER_ID to a simple name.
    // Do not use the URN here, as we are now intercepting the standard password grant.
    public static final String PROVIDER_ID = "bootstrap-token-grant";

    private static final BootstrapTokenGrantAuthenticator SINGLETON = new BootstrapTokenGrantAuthenticator();

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        // This is the name you will see in the Keycloak Admin Console 'Add Step' list
        return "Opex Bootstrap Interceptor";
    }

    @Override
    public String getReferenceCategory() {
        // Keep this as "grant" so it appears in the Direct Grant flow options
        return "grant";
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[] {
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.ALTERNATIVE,
                AuthenticationExecutionModel.Requirement.DISABLED
        };
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Intercepts standard password grant to exchange a bootstrap_token for full tokens";
    }

    @Override
    public void init(Config.Scope config) {}

    @Override
    public void postInit(KeycloakSessionFactory factory) {}

    @Override
    public void close() {}

    @Override
    public int order() {
        return 0;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }
}