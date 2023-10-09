FROM vault:1.11.7
COPY [ "workflow-vault.sh", "backend-policy.hcl", "panel-policy.hcl", "vault.json",  "/vault/config/"]
#COPY --chmod=777 workflow-vault.sh /vault/config/
EXPOSE 8200
#RUN ["chmod", "+x", "/vault/config/workflow-vault.sh"]
RUN sed -i 's/\r$//' /vault/config/workflow-vault.sh  && \
        chmod +x /vault/config/workflow-vault.sh
ENTRYPOINT /vault/config/workflow-vault.sh
HEALTHCHECK --interval=15s --start-period=15s --retries=15 CMD wget -qO- http://localhost:8200/v1/sys/health &>/dev/null || exit 1