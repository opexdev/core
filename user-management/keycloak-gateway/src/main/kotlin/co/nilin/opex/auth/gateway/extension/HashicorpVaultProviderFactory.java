package co.nilin.opex.auth.gateway.extension;

import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.vault.VaultNotFoundException;
import org.keycloak.vault.VaultProvider;
import org.keycloak.vault.VaultProviderFactory;

public class HashicorpVaultProviderFactory implements VaultProviderFactory {
   private static final Logger logger = Logger.getLogger(HashicorpVaultProviderFactory.class);

   public static final String PROVIDER_ID = "hachicorp-vault";

   private String vaultAppId;
   private String vaultUserId;
   private String vaultUrl;
   private String vaultSecretEngineName;

   @Override
   public VaultProvider create(KeycloakSession session) {
      VaultService service = new VaultService(session);
      if (!service.isVaultAvailable(vaultUrl, vaultAppId, vaultUserId)) {
         logger.error("Vault unavailable : " + vaultUrl);
         throw new VaultNotFoundException("Vault unavailable : " + vaultUrl);
      } else {
         logger.info("Vault available : " + vaultUrl);
      }
      return new HashicorpVaultProvider(vaultUrl, vaultAppId, vaultUserId, session.getContext().getRealm().getName(), vaultSecretEngineName, service);

   }

   private static String format(String url) {
      if (!(url.charAt(url.length() - 1) == '/')) {
         return url.concat("/");
      } else {
         return url;
      }
   }

   @Override
   public void init(Scope config) {
      if (System.getenv("BACKEND_APP") != null) {
         vaultAppId = System.getenv("BACKEND_APP");
      } else {
         vaultAppId = config.get("appId");
      }
      if (System.getenv("BACKEND_USER") != null) {
         vaultUserId = System.getenv("BACKEND_USER");
      } else {
         vaultUserId = config.get("userId");
      }
      vaultUrl = config.get("url") != null ? format(config.get("url")) : null;
      vaultSecretEngineName = config.get("engine-name");
      logger.info("Init Hashicorp: " + vaultUrl);
   }

   @Override
   public void postInit(KeycloakSessionFactory factory) {
      // TODO Auto-generated method stub

   }

   @Override
   public void close() {
      // TODO Auto-generated method stub

   }

   @Override
   public String getId() {
      return PROVIDER_ID;
   }

}
