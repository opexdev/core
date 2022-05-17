FROM vault:1.10.1
COPY ["backend-policy.hcl", "panel-policy.hcl", "vault.json", "workflow-vault.sh",  "/vault/config/"]
EXPOSE 8200
ENTRYPOINT /vault/config/workflow-vault.sh
HEALTHCHECK --interval=15s --start-period=15s --retries=15 CMD wget -qO- http://localhost:8200/v1/sys/health &>/dev/null || exit 1
