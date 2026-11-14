ALTER TABLE company ADD COLUMN company_uuid UUID;

UPDATE company SET company_uuid = gen_random_uuid() WHERE company_uuid IS NULL;

ALTER TABLE company ALTER COLUMN company_uuid SET NOT NULL;
ALTER TABLE company ADD CONSTRAINT uq_company_company_uuid UNIQUE (company_uuid);
