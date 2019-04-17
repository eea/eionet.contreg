
-- ---------------------------------------------------------------------
-- Drop triggers before running the below expensive operations.
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

ALTER TABLE CR.cr3user.harvest_source ADD COLUMN "last_harvest_code" INTEGER;
UPDATE CR.cr3user.harvest_source hs
SET last_harvest_code = (
    SELECT http_code AS last_harvest_code
    FROM CR.cr3user.harvest h
    WHERE h.harvest_id = hs.last_harvest_id);

-- ---------------------------------------------------------------------
-- Run deletions of redirecting harvest sources.
-- ---------------------------------------------------------------------

DELETE FROM CR.cr3user.harvest_source WHERE last_harvest_code IN (301,302,303,307,308);
DELETE FROM CR.cr3user.harvest WHERE harvest_source_id NOT IN (SELECT harvest_source_id FROM CR.cr3user.harvest_source);
DELETE FROM CR.cr3user.harvest_message WHERE harvest_id NOT IN (SELECT harvest_id FROM CR.cr3user.harvest);
DELETE FROM CR.cr3user.endpoint_harvest_query WHERE endpoint_url_hash NOT IN (SELECT url_hash FROM CR.cr3user.harvest_source);
DELETE FROM CR.cr3user.post_harvest_script WHERE target_source_url NOT IN (SELECT url FROM CR.cr3user.harvest_source);
DELETE FROM CR.cr3user.urgent_harvest_queue WHERE url NOT IN (SELECT url FROM CR.cr3user.harvest_source);

-- ---------------------------------------------------------------------
-- Re-create triggers.
-- ---------------------------------------------------------------------

CREATE TRIGGER CR_CR3USER_HARVEST_MESSAGE_FK_CHECK_INSERT before insert on "CR"."cr3user"."harvest_message" order 99 referencing new as N { if ('ON' <> registry_get ('FK_UNIQUE_CHEK')) return;

 DECLARE _VAR_harvest_id ANY;
 _VAR_harvest_id := N."harvest_id";
if (_VAR_harvest_id IS NOT NULL and   not exists (select 1 from "CR"."cr3user"."harvest" WHERE "harvest_id" = _VAR_harvest_id))
signal ('S1000','INSERT statement conflicted with FOREIGN KEY constraint referencing table "CR.cr3user.harvest"', 'SR306');

}
;

CREATE TRIGGER CR_CR3USER_HARVEST_MESSAGE_FK_CHECK_UPDATE before update on "CR"."cr3user"."harvest_message" order 99 REFERENCING OLD AS O, NEW AS N { if ('ON' <> registry_get ('FK_UNIQUE_CHEK')) return;
if (N."harvest_id" IS NOT NULL and   not exists (select 1 from "CR"."cr3user"."harvest" WHERE "harvest_id" = N."harvest_id"))
signal ('S1000','UPDATE statement conflicted with FOREIGN KEY constraint referencing table "CR.cr3user.harvest"', 'SR307');

}
;

CREATE TRIGGER CR_CR3USER_HARVEST_FK_CHECK_INSERT before insert on "CR"."cr3user"."harvest" order 99 referencing new as N { if ('ON' <> registry_get ('FK_UNIQUE_CHEK')) return;

 DECLARE _VAR_harvest_source_id ANY;
 _VAR_harvest_source_id := N."harvest_source_id";
if (_VAR_harvest_source_id IS NOT NULL and   not exists (select 1 from "CR"."cr3user"."harvest_source" WHERE "harvest_source_id" = _VAR_harvest_source_id))
signal ('S1000','INSERT statement conflicted with FOREIGN KEY constraint referencing table "CR.cr3user.harvest_source"', 'SR306');

}
;

CREATE TRIGGER CR_CR3USER_HARVEST_FK_CHECK_UPDATE before update on "CR"."cr3user"."harvest" order 99 REFERENCING OLD AS O, NEW AS N { if ('ON' <> registry_get ('FK_UNIQUE_CHEK')) return;
if (N."harvest_source_id" IS NOT NULL and   not exists (select 1 from "CR"."cr3user"."harvest_source" WHERE "harvest_source_id" = N."harvest_source_id"))
signal ('S1000','UPDATE statement conflicted with FOREIGN KEY constraint referencing table "CR.cr3user.harvest_source"', 'SR307');

}
;

CREATE TRIGGER CR_CR3USER_HARVEST_PK_CHECK_UPDATE BEFORE UPDATE ON "CR"."cr3user"."harvest" order 99 REFERENCING OLD AS O, NEW AS N {
 if ('ON' <> registry_get ('FK_UNIQUE_CHEK'))
	 return;
if ((N."harvest_id" <> O."harvest_id") and  exists (select 1 from "CR"."cr3user"."harvest_message" WHERE "harvest_id" = O."harvest_id"))
signal ('S1000','UPDATE statement conflicted with COLUMN REFERENCE constraint "hame_ha_fk"', 'SR305');

}
;

CREATE TRIGGER "CR_cr3user_harvest_FK_DELETE" AFTER DELETE
 ON "CR"."cr3user"."harvest" ORDER 99 referencing old as O {
 DECLARE EXIT HANDLER FOR SQLSTATE '*' { ROLLBACK WORK; RESIGNAL; };
  DECLARE _VAR_harvest_id VARCHAR;
 _VAR_harvest_id := O."harvest_id";
   DELETE FROM "CR"."cr3user"."harvest_message"  WHERE "harvest_id" = _VAR_harvest_id;

};

CREATE TRIGGER CR_CR3USER_HARVEST_SOURCE_PK_CHECK_UPDATE BEFORE UPDATE ON "CR"."cr3user"."harvest_source" order 99 REFERENCING OLD AS O, NEW AS N {
 if ('ON' <> registry_get ('FK_UNIQUE_CHEK'))
	 return;
if ((N."harvest_source_id" <> O."harvest_source_id") and  exists (select 1 from "CR"."cr3user"."harvest" WHERE "harvest_source_id" = O."harvest_source_id"))
signal ('S1000','UPDATE statement conflicted with COLUMN REFERENCE constraint "ha_haso_fk"', 'SR305');

}
;

CREATE TRIGGER "CR_cr3user_harvest_source_FK_DELETE" AFTER DELETE
 ON "CR"."cr3user"."harvest_source" ORDER 99 referencing old as O {
 DECLARE EXIT HANDLER FOR SQLSTATE '*' { ROLLBACK WORK; RESIGNAL; };
  DECLARE _VAR_url_hash VARCHAR;
 _VAR_url_hash := O."url_hash";
 DECLARE _VAR_harvest_source_id VARCHAR;
 _VAR_harvest_source_id := O."harvest_source_id";
   DELETE FROM "CR"."cr3user"."endpoint_harvest_query"  WHERE "endpoint_url_hash" = _VAR_url_hash;
  DELETE FROM "CR"."cr3user"."harvest"  WHERE "harvest_source_id" = _VAR_harvest_source_id;

};

CREATE TRIGGER "CR_cr3user_harvest_source_FK_UPDATE" AFTER UPDATE ("url_hash", "harvest_source_id")
 ON "CR"."cr3user"."harvest_source" ORDER 99 REFERENCING OLD AS O, NEW AS N {
 DECLARE EXIT HANDLER FOR SQLSTATE '*' { ROLLBACK WORK; RESIGNAL; };
   UPDATE "CR"."cr3user"."endpoint_harvest_query" SET "endpoint_url_hash" = N."url_hash" WHERE "endpoint_url_hash" = O."url_hash";

 };

CREATE TRIGGER CR_CR3USER_POST_HARVEST_SCRIPT_FK_CHECK_INSERT before insert on "CR"."cr3user"."post_harvest_script" order 99 referencing new as N { if ('ON' <> registry_get ('FK_UNIQUE_CHEK')) return;

 DECLARE _VAR_external_service_id ANY;
 _VAR_external_service_id := N."external_service_id";
if (_VAR_external_service_id IS NOT NULL and   not exists (select 1 from "CR"."cr3user"."external_service" WHERE "service_id" = _VAR_external_service_id))
signal ('S1000','INSERT statement conflicted with FOREIGN KEY constraint referencing table "CR.cr3user.external_service"', 'SR306');

}
;

CREATE TRIGGER CR_CR3USER_POST_HARVEST_SCRIPT_FK_CHECK_UPDATE before update on "CR"."cr3user"."post_harvest_script" order 99 REFERENCING OLD AS O, NEW AS N { if ('ON' <> registry_get ('FK_UNIQUE_CHEK')) return;
if (N."external_service_id" IS NOT NULL and   not exists (select 1 from "CR"."cr3user"."external_service" WHERE "service_id" = N."external_service_id"))
signal ('S1000','UPDATE statement conflicted with FOREIGN KEY constraint referencing table "CR.cr3user.external_service"', 'SR307');

}
;

CREATE TRIGGER CR_CR3USER_ENDPOINT_HARVEST_QUERY_FK_CHECK_INSERT before insert on "CR"."cr3user"."endpoint_harvest_query" order 99 referencing new as N { if ('ON' <> registry_get ('FK_UNIQUE_CHEK')) return;

 DECLARE _VAR_endpoint_url_hash ANY;
 _VAR_endpoint_url_hash := N."endpoint_url_hash";
if (_VAR_endpoint_url_hash IS NOT NULL and   not exists (select 1 from "CR"."cr3user"."harvest_source" WHERE "url_hash" = _VAR_endpoint_url_hash))
signal ('S1000','INSERT statement conflicted with FOREIGN KEY constraint referencing table "CR.cr3user.harvest_source"', 'SR306');

}
;

CREATE TRIGGER CR_CR3USER_ENDPOINT_HARVEST_QUERY_FK_CHECK_UPDATE before update on "CR"."cr3user"."endpoint_harvest_query" order 99 REFERENCING OLD AS O, NEW AS N { if ('ON' <> registry_get ('FK_UNIQUE_CHEK')) return;
if (N."endpoint_url_hash" IS NOT NULL and   not exists (select 1 from "CR"."cr3user"."harvest_source" WHERE "url_hash" = N."endpoint_url_hash"))
signal ('S1000','UPDATE statement conflicted with FOREIGN KEY constraint referencing table "CR.cr3user.harvest_source"', 'SR307');

}
;

CREATE TRIGGER CR_CR3USER_EXTERNAL_SERVICE_PK_CHECK_UPDATE BEFORE UPDATE ON "CR"."cr3user"."external_service" order 99 REFERENCING OLD AS O, NEW AS N {
 if ('ON' <> registry_get ('FK_UNIQUE_CHEK'))
	 return;
if ((N."service_id" <> O."service_id") and  exists (select 1 from "CR"."cr3user"."post_harvest_script" WHERE "external_service_id" = O."service_id"))
signal ('S1000','UPDATE statement conflicted with COLUMN REFERENCE constraint "post_harvest_script_external_service_external_service_id_service_id"', 'SR305');

}
;

CREATE TRIGGER "CR_cr3user_external_service_FK_DELETE" AFTER DELETE
 ON "CR"."cr3user"."external_service" ORDER 99 referencing old as O {
 DECLARE EXIT HANDLER FOR SQLSTATE '*' { ROLLBACK WORK; RESIGNAL; };
  DECLARE _VAR_service_id VARCHAR;
 _VAR_service_id := O."service_id";
   DELETE FROM "CR"."cr3user"."post_harvest_script"  WHERE "external_service_id" = _VAR_service_id;

};
