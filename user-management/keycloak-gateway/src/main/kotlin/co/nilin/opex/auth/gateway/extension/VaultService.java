package co.nilin.opex.auth.gateway.extension;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.jboss.logging.Logger;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.models.KeycloakSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * VaultService
 */
public class VaultService {

    private final KeycloakSession session;
    private static final Logger logger = Logger.getLogger(VaultService.class);

    public VaultService(KeycloakSession session) {
        this.session = session;
    }

    static class UserId {
        @JsonProperty("user_id")
        public String userId;

        public UserId(String userId) {
            this.userId = userId;
        }
    }

    public ByteBuffer getSecretFromVault(String vaultUrl, String realm, String vaultSecretEngineName, String secretName, String vaultAppId, String vaultUserId, int secretVersion) {
        try {
            //curl \    --method POST \    --data '{"user_id": ":user_id"}' \    http://127.0.0.1:8200/v1/auth/app-id/login/:app_id
            String vaultToken = SimpleHttp.doPost(vaultUrl + "v1//auth/app-id/login/" + vaultAppId, session).json(new UserId(vaultUserId)).asJson().get("auth").get("client_token").textValue();
            JsonNode node = SimpleHttp.doGet(vaultUrl + "v1/" + vaultSecretEngineName + "/" + realm + "?version=" + secretVersion, session).header("X-Vault-Token", vaultToken).asJson();
            byte[] secretBytes = node.get("data").get(secretName).textValue().getBytes(StandardCharsets.UTF_8);
            return ByteBuffer.wrap(secretBytes);
        } catch (IOException e) {
            logger.error("secret not available", e);
            return null;
        }
    }

    public boolean isVaultAvailable(String vaultUrl, String vaultAppId, String vaultUserId) {
        String healthVaultUrl = vaultUrl + "v1/sys/health";
        try {
            JsonNode vaultHealthResponseNode = SimpleHttp.doGet(healthVaultUrl, session).asJson();
            boolean vaultIsInitialized = vaultHealthResponseNode.get("initialized").asBoolean();
            boolean vaultIsSealed = vaultHealthResponseNode.get("sealed").asBoolean();
            return (vaultIsInitialized && !vaultIsSealed);
        } catch (IOException e) {
            logger.error("vault service unavailable", e);
            return false;
        }
    }

}