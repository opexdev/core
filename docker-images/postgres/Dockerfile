FROM postgres:14-alpine
COPY ["add-backup-user.sh", "/docker-entrypoint-initdb.d/"]
EXPOSE 5432
HEALTHCHECK --interval=15s --start-period=30s --retries=15 CMD pg_isready -U $POSTGRES_USER -d $POSTGRES_DB -q || exit 1
