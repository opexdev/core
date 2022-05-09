#!/bin/sh
set -em

## Export environment variables
export VAULT_SKIP_VERIFY='true'

vault server -config /vault/config/vault.json &

unseal() {
  ## Parse unsealed keys
  (grep "Unseal Key " < /vault/file/generated_keys.txt | cut -c15-) > /vault/file/keys.txt

  while IFS= read -r line; do
      vault operator unseal $line
  done < /vault/file/keys.txt

  ## Get root token
  (grep "Initial Root Token: " < /vault/file/generated_keys.txt | cut -c21-) > /vault/file/tokens.txt
  while IFS= read -r line; do
      export VAULT_TOKEN=${line}
  done < /vault/file/tokens.txt
}

init_secrets() {
  ## Enable kv
  vault secrets enable -path=secret -version=1 kv

  ## Enable user/pass and add default user
  vault auth enable userpass

  ## Enable panel policies
  vault policy write panel-policy /vault/config/panel-policy.hcl

  ## Set password
  vault write auth/userpass/users/admin password=${PANEL_PASS} policies=panel-policy

  ## Check login user/pass
  vault login -method=userpass username=admin password=${PANEL_PASS}

  ## Enable app-id and add default user-id
  vault auth enable app-id

  ## Enable backend policies
  vault policy write backend-policy /vault/config/backend-policy.hcl

  ## Enable backend apps
  vault write auth/app-id/map/app-id/opex-accountant value=backend-policy display_name=opex-accountant
  vault write auth/app-id/map/app-id/opex-api value=backend-policy display_name=opex-api
  vault write auth/app-id/map/app-id/opex-bc-gateway value=backend-policy display_name=opex-bc-gateway
  vault write auth/app-id/map/app-id/opex-eventlog value=backend-policy display_name=opex-eventlog
  vault write auth/app-id/map/app-id/opex-auth value=backend-policy display_name=opex-auth
  vault write auth/app-id/map/app-id/opex-wallet value=backend-policy display_name=opex-wallet
  vault write auth/app-id/map/app-id/opex-websocket value=backend-policy display_name=opex-websocket
  vault write auth/app-id/map/app-id/opex-payment value=backend-policy display_name=opex-payment
  vault write auth/app-id/map/app-id/opex-admin value=backend-policy display_name=opex-admin
  vault write auth/app-id/map/app-id/chain-scan-gateway value=backend-policy display_name=chain-scan-gateway
  vault write auth/app-id/map/app-id/opex-referral value=backend-policy display_name=opex-referral

  ## Enable user-id
  vault write auth/app-id/map/user-id/${BACKEND_USER} value=opex-wallet,opex-websocket,opex-eventlog,opex-auth,opex-accountant,opex-api,opex-bc-gateway,opex-payment,opex-admin,chain-scan-gateway,opex-referral

  ## Check login app-id
  vault write auth/app-id/login/opex-accountant user_id=${BACKEND_USER}
  vault write auth/app-id/login/opex-api user_id=${BACKEND_USER}
  vault write auth/app-id/login/opex-bc-gateway user_id=${BACKEND_USER}
  vault write auth/app-id/login/opex-eventlog user_id=${BACKEND_USER}
  vault write auth/app-id/login/opex-auth user_id=${BACKEND_USER}
  vault write auth/app-id/login/opex-wallet user_id=${BACKEND_USER}
  vault write auth/app-id/login/opex-websocket user_id=${BACKEND_USER}
  vault write auth/app-id/login/opex-payment user_id=${BACKEND_USER}
  vault write auth/app-id/login/opex-admin user_id=${BACKEND_USER}
  vault write auth/app-id/login/chain-scan-gateway user_id=${BACKEND_USER}
  vault write auth/app-id/login/opex-referral user_id=${BACKEND_USER}

  ## Add secret values
  vault kv put secret/opex smtppass=${SMTP_PASS}
  vault kv put secret/opex-accountant dbusername=${DB_USER} dbpassword=${DB_PASS} db_backup_username=${DB_BACKUP_USER} db_backup_pass=${DB_BACKUP_PASS}
  vault kv put secret/opex-api dbusername=${DB_USER} dbpassword=${DB_PASS} db_backup_username=${DB_BACKUP_USER} db_backup_pass=${DB_BACKUP_PASS}
  vault kv put secret/opex-bc-gateway dbusername=${DB_USER} dbpassword=${DB_PASS} db_backup_username=${DB_BACKUP_USER} db_backup_pass=${DB_BACKUP_PASS}
  vault kv put secret/opex-eventlog dbusername=${DB_USER} dbpassword=${DB_PASS} db_backup_username=${DB_BACKUP_USER} db_backup_pass=${DB_BACKUP_PASS}
  vault kv put secret/opex-auth dbusername=${DB_USER} dbpassword=${DB_PASS} admin_username=${KEYCLOAK_ADMIN_USERNAME} admin_password=${KEYCLOAK_ADMIN_PASSWORD}
  vault kv put secret/opex-wallet dbusername=${DB_USER} dbpassword=${DB_PASS} db_backup_username=${DB_BACKUP_USER} db_backup_pass=${DB_BACKUP_PASS}
  vault kv put secret/opex-websocket dbusername=${DB_USER} dbpassword=${DB_PASS} db_backup_username=${DB_BACKUP_USER} db_backup_pass=${DB_BACKUP_PASS}
  vault kv put secret/opex-payment dbusername=${DB_USER} dbpassword=${DB_PASS} db_backup_username=${DB_BACKUP_USER} db_backup_pass=${DB_BACKUP_PASS} vandar_api_key=${VANDAR_API_KEY}
  vault kv put secret/opex-admin keycloak_client_secret=${OPEX_ADMIN_KEYCLOAK_CLIENT_SECRET}
  vault kv put secret/chain-scan-gateway dbusername=${DB_USER} dbpassword=${DB_PASS}
  vault kv put secret/opex-referral dbusername=${DB_USER} dbpassword=${DB_PASS} db_backup_username=${DB_BACKUP_USER} db_backup_pass=${DB_BACKUP_PASS}
}

# Wait for server to initialize
sleep 10
if [ ! -f /vault/file/generated_keys.txt ]; then
  ## Generate keys
  vault operator init > /vault/file/generated_keys.txt
  unseal
  init_secrets
else
  unseal
fi

## Keep alive
fg %1
