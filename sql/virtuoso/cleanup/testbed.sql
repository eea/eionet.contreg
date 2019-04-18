
-- ---------------------------------------------------------------------
-- Drop triggers before running the below operations.
-- ---------------------------------------------------------------------

DROP TRIGGER CR.cr3user.CR_CR3USER_HARVEST_MESSAGE_FK_CHECK_INSERT;
DROP TRIGGER CR.cr3user.CR_CR3USER_HARVEST_MESSAGE_FK_CHECK_UPDATE;
DROP TRIGGER CR.cr3user.CR_CR3USER_HARVEST_FK_CHECK_INSERT;
DROP TRIGGER CR.cr3user.CR_CR3USER_HARVEST_FK_CHECK_UPDATE;
DROP TRIGGER CR.cr3user.CR_CR3USER_HARVEST_PK_CHECK_UPDATE;
DROP TRIGGER CR.cr3user.CR_cr3user_harvest_FK_DELETE;
DROP TRIGGER CR.cr3user.CR_CR3USER_HARVEST_SOURCE_PK_CHECK_UPDATE;
DROP TRIGGER CR.cr3user.CR_cr3user_harvest_source_FK_DELETE;
DROP TRIGGER CR.cr3user.CR_cr3user_harvest_source_FK_UPDATE;
DROP TRIGGER CR.cr3user.CR_CR3USER_POST_HARVEST_SCRIPT_FK_CHECK_INSERT;
DROP TRIGGER CR.cr3user.CR_CR3USER_POST_HARVEST_SCRIPT_FK_CHECK_UPDATE;
DROP TRIGGER CR.cr3user.CR_CR3USER_ENDPOINT_HARVEST_QUERY_FK_CHECK_INSERT;
DROP TRIGGER CR.cr3user.CR_CR3USER_ENDPOINT_HARVEST_QUERY_FK_CHECK_UPDATE;
DROP TRIGGER CR.cr3user.CR_CR3USER_EXTERNAL_SERVICE_PK_CHECK_UPDATE;
DROP TRIGGER CR.cr3user.CR_cr3user_external_service_FK_DELETE;

-- ---------------------------------------------------------------------
-- Ensure we have no spaces in harvest_source urls, as below IRI functions will fail otherwise.
-- ---------------------------------------------------------------------

UPDATE CR.cr3user.harvest_source SET url = replace(url, ' ', '%20') WHERE url LIKE '% %';

-- ---------------------------------------------------------------------
-- Replace HTTPS sources with HTTP.
-- ---------------------------------------------------------------------
UPDATE CR.cr3user.harvest_source SET url = replace(url, 'https://', 'http://') WHERE url LIKE 'https://%';
UPDATE CR.cr3user.post_harvest_script SET target_source_url = replace(target_source_url, 'https://', 'http://') WHERE target_source_url LIKE 'https://%';

-- ---------------------------------------------------------------------
-- Drop foreign keys pointing to harvest_source_id or url_hash of harvest_source table.
-- Otherwise we cannot modify url_hash values below.
-- ---------------------------------------------------------------------

ALTER TABLE "CR"."cr3user"."endpoint_harvest_query" DROP CONSTRAINT "fk_url_hash";
ALTER TABLE "CR"."cr3user"."harvest" DROP CONSTRAINT "ha_haso_fk";

-- ---------------------------------------------------------------------
-- Temporarily set harvest_source primary key to harvest_source_id, so we can update url_hash values.
-- ---------------------------------------------------------------------
ALTER TABLE "CR"."cr3user"."harvest_source" MODIFY PRIMARY KEY ("harvest_source_id");

-- ---------------------------------------------------------------------
-- Update url_hash values to IRI numbers (as opposed to Java-generated hashes they used to be).
-- ---------------------------------------------------------------------
UPDATE CR.cr3user.harvest_source SET url_hash = iri_id_num(iri_to_id(url));
UPDATE CR.cr3user.endpoint_harvest_query SET endpoint_url_hash = iri_id_num(iri_to_id(endpoint_url));

-- ---------------------------------------------------------------------
-- Set harvest_source primary key back to url_hash.
-- ---------------------------------------------------------------------
ALTER TABLE "CR"."cr3user"."harvest_source" MODIFY PRIMARY KEY ("url_hash");

-- ---------------------------------------------------------------------
-- Re-create foreign keys we dropped above.
-- ---------------------------------------------------------------------
ALTER TABLE "CR"."cr3user"."harvest"
  ADD CONSTRAINT "ha_haso_fk" FOREIGN KEY ("harvest_source_id")
    REFERENCES "CR"."cr3user"."harvest_source" ("harvest_source_id") ON DELETE CASCADE;
ALTER TABLE "CR"."cr3user"."endpoint_harvest_query"
  ADD CONSTRAINT "fk_url_hash" FOREIGN KEY ("endpoint_url_hash")
    REFERENCES "CR"."cr3user"."harvest_source" ("url_hash") ON UPDATE CASCADE ON DELETE CASCADE;

-----

DELETE FROM CR.cr3user.urgent_harvest_queue WHERE url NOT IN (SELECT url FROM CR.cr3user.harvest_source);

----

UPDATE DB.DBA.RDF_QUAD TABLE OPTION (index RDF_QUAD_POGS)
SET g = iri_to_id (replace(id_to_iri(g), 'https://', 'http://'))
WHERE starts_with(id_to_iri(g), 'https://')=1;


UPDATE CR.cr3user.harvest_source SET url = replace(url, 'https://', 'http://') WHERE url LIKE 'https://%';