package co.nilin.opex.auth.gateway.extension;

import org.jboss.logging.Logger;
import org.keycloak.vault.DefaultVaultRawSecret;
import org.keycloak.vault.VaultProvider;
import org.keycloak.vault.VaultRawSecret;

import java.util.Optional;

/**
 * HashicorpVaultProviderFactory
 */
public class HashicorpVaultProvider implements VaultProvider {
    private static final Logger logger = Logger.getLogger(HashicorpVaultProviderFactory.class);

    private String vaultUrl;
    private String vaultAppId;
    private String vaultUserId;
    private String realmName;
    private String vaultSecretEngineName;
    private VaultService service;

    @Override
    public VaultRawSecret obtainSecret(String vaultSecretId) {
        int secretVersion = 0;
        String vaultSecretName = vaultSecretId;
        if (vaultSecretId.contains(":")) {
            try {
                secretVersion = Integer.parseInt(vaultSecretId.substring(vaultSecretId.lastIndexOf(":") + 1));
                vaultSecretName = vaultSecretId.substring(0, vaultSecretId.lastIndexOf(":"));
            } catch (NumberFormatException e) {
                logger.error("last string after : is expected to be the version number");
            }
        }

        return DefaultVaultRawSecret.forBuffer(Optional.of(service.getSecretFromVault(vaultUrl, realmName, vaultSecretEngineName, vaultSecretName, vaultAppId, vaultUserId, secretVersion)));
    }

    @Override
    public void close() {
    }

    public HashicorpVaultProvider(String vaultUrl, String vaultAppId, String vaultUserId, String realmName, String vaultSecretEngineName, VaultService service) {
        this.vaultUrl = vaultUrl;
        this.vaultAppId = vaultAppId;
        this.vaultUserId = vaultUserId;
        this.realmName = realmName;
        this.vaultSecretEngineName = vaultSecretEngineName;
        this.service = service;
    }

}