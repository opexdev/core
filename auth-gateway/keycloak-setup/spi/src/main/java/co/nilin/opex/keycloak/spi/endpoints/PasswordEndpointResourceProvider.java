package co.nilin.opex.keycloak.spi.endpoints;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.services.resource.RealmResourceProvider;

import java.util.Map;

public class PasswordEndpointResourceProvider implements RealmResourceProvider {

    private final KeycloakSession session;

    public PasswordEndpointResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return this;
    }

    @Override
    public void close() {

    }

    @POST
    @Path("/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validatePassword(Map<String, String> payload) {
        var userId = payload.get("userId");
        var password = payload.get("password");
        if (userId == null || password == null)
            return Response.status(Response.Status.BAD_REQUEST).build();

        var realm = session.getContext().getRealm();
        var user = session.users().getUserById(realm, userId);
        if (user == null)
            return Response.status(Response.Status.NOT_FOUND).build();

        var input = new UserCredentialModel();
        input.setType(PasswordCredentialModel.TYPE);
        input.setValue(password);

        var isValid = user.credentialManager().isValid(input);
        if (isValid)
            return Response.ok(Map.of("valid",true)).build();
        else
            return Response.status(Response.Status.UNAUTHORIZED).build();
    }
}