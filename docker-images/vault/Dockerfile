FROM vault:1.10.1
COPY ["backend-policy.hcl", "panel-policy.hcl", "vault.json", "workflow-vault.sh",  "/vault/config/"]
EXPOSE 8200
ENTRYPOINT /vault/config/workflow-vault.sh
