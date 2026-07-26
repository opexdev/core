package co.nilin.opex.keycloak.spi;

import org.keycloak.TokenVerifier;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.*;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;
import org.keycloak.representations.AccessToken;

import java.util.*;

public class BootstrapTokenGrantAuthenticator implements Authenticator {

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> params = context.getHttpRequest().getDecodedFormParameters();
        String bootstrapTokenString = params.getFirst("bootstrap_token");

        if (bootstrapTokenString == null || bootstrapTokenString.isEmpty()) {

            System.out.println("No bootstrap token found, skipping to next authenticator.");

            String username = context.getHttpRequest().getDecodedFormParameters().getFirst("username");
            if (username != null) {
                UserModel user = context.getSession().users().getUserByUsername(context.getRealm(), username);
                if (user != null) {
                    context.setUser(user); // Attach the user so the next step (Password) knows who to check
                }
            }
            context.attempted();
            return;
        }
        try {
            // Parse the JWT to get the user ID (the 'sub' claim)
            AccessToken token = TokenVerifier.create(bootstrapTokenString, AccessToken.class).getToken();
            String userId = token.getSubject();

            KeycloakSession session = context.getSession();
            RealmModel realm = context.getRealm();

            // Find the actual user from the database
            UserModel user = session.users().getUserById(realm, userId);

            if (user == null || !user.isEnabled()) {
                sendError(context, "invalid_grant");
                return;
            }

            // IMPORTANT: Identify the user and tell Keycloak this step is finished successfully
            context.setUser(user);
            context.success();

        } catch (Exception e) {
            // This happens if the JWT is malformed or expired
            sendError(context, "invalid_grant");
        }
    }

    private void sendError(AuthenticationFlowContext context, String errorCode) {
        Map<String, String> errorEntity = new HashMap<>();
        errorEntity.put("error", errorCode);

        Response response = Response.status(Response.Status.BAD_REQUEST)
                .entity(errorEntity)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();

        context.failure(AuthenticationFlowError.UNKNOWN_USER, response);
    }

    @Override public void action(AuthenticationFlowContext context) {}
    @Override public boolean requiresUser() { return false; }
    @Override public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) { return true; }
    @Override public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {}
    @Override public void close() {}
}