ALTER TABLE currency_on_chain_gateway ADD COLUMN applicator VARCHAR(80);
ALTER TABLE currency_on_chain_gateway_localization ADD COLUMN applicator VARCHAR(80);
ALTER TABLE chains ADD COLUMN applicator VARCHAR(80);
ALTER TABLE address_types ADD COLUMN applicator VARCHAR(80);
ALTER TABLE reserved_addresses ADD COLUMN applicator VARCHAR(80);
ALTER TABLE assigned_addresses ADD COLUMN applicator VARCHAR(80);