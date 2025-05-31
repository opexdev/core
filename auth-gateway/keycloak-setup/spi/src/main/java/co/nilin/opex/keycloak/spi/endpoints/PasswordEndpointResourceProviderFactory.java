package co.nilin.opex.keycloak.spi.endpoints;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

public class PasswordEndpointResourceProviderFactory implements RealmResourceProviderFactory {

    public static final String ID = "password";

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new PasswordEndpointResourceProvider(session);
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return ID;
    }
}
