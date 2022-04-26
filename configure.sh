#!/bin/bash

docker run \
-e PGPASSWORD="${DB_PASS:-hiopex}" \
--network="${DEFAULT_NETWORK_NAME:-opex}" \
--rm \
--mount type=bind,src="$PWD/bc-gateway-data.sql",dst=/data.sql \
--entrypoint="" \
postgres:14-alpine \
psql -h postgres-bc-gateway -p 5432 -U "${DB_USER:-opex}" -d opex_bc_gateway -f /data.sql
