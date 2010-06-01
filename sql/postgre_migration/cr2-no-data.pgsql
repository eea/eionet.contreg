
CREATE TABLE harvest (
  HARVEST_ID serial,
  HARVEST_SOURCE_ID integer NOT NULL,
  TYPE varchar(20) DEFAULT '' NOT NULL,
  USERNAME varchar(45) DEFAULT NULL,
  STATUS varchar(10) DEFAULT '' NOT NULL,
  STARTED timestamp DEFAULT NULL,
  FINISHED timestamp DEFAULT NULL,
  TOT_STATEMENTS integer DEFAULT NULL,
  LIT_STATEMENTS integer DEFAULT NULL,
  RES_STATEMENTS integer DEFAULT NULL,
  ENC_SCHEMES integer DEFAULT NULL,
  TOT_RESOURCES integer DEFAULT NULL
);
ALTER TABLE harvest ADD PRIMARY KEY (harvest_id);
ALTER TABLE harvest ADD CONSTRAINT harvest_unique UNIQUE (harvest_source_id, started);



CREATE TABLE harvest_message (
  HARVEST_MESSAGE_ID serial,
  HARVEST_ID integer NOT NULL,
  TYPE varchar(3) DEFAULT '' NOT NULL,
  MESSAGE varchar(255) DEFAULT '' NOT NULL,
  STACK_TRACE text
);
ALTER TABLE harvest_message ADD PRIMARY KEY (harvest_message_id);
CREATE INDEX harvest_message_harvest_id ON harvest_message (harvest_id);



CREATE TYPE ynboolean AS ENUM ('Y', 'N');
CREATE TABLE harvest_source (
  HARVEST_SOURCE_ID serial,
  URL_HASH bigint NOT NULL,
  URL varchar(1024) NOT NULL,
  TRACKED_FILE ynboolean DEFAULT 'N' NOT NULL,
  EMAILS varchar(255) DEFAULT NULL,
  TIME_CREATED timestamp NOT NULL,
  STATEMENTS integer DEFAULT NULL,
  RESOURCES integer DEFAULT NULL,
  COUNT_UNAVAIL integer DEFAULT '0' NOT NULL,
  LAST_HARVEST timestamp DEFAULT NULL,
  INTERVAL_MINUTES integer DEFAULT '0' NOT NULL,
  SOURCE bigint DEFAULT '0' NOT NULL,
  GEN_TIME bigint DEFAULT '0' NOT NULL,
  LAST_HARVEST_FAILED ynboolean DEFAULT 'N' NOT NULL
);
ALTER TABLE harvest_source ADD PRIMARY KEY (harvest_source_id);
ALTER TABLE harvest_source ADD CONSTRAINT harvest_source_unique UNIQUE (url_hash);

CREATE RULE skip_duplicate_harvest_source AS
    ON INSERT TO harvest_source
    WHERE
      EXISTS(SELECT 1 FROM harvest_source WHERE url_hash=NEW.url_hash)
    DO INSTEAD (UPDATE harvest_source SET url_hash=NEW.url_hash where url_hash=NEW.url_hash);


CREATE TABLE remove_source_queue (
  URL varchar(250) NOT NULL
);
ALTER TABLE remove_source_queue ADD PRIMARY KEY (url);

CREATE RULE skip_duplicate_remove_source AS
    ON INSERT TO remove_source_queue
    WHERE
      EXISTS(SELECT 1 FROM remove_source_queue WHERE url=NEW.url)
    DO INSTEAD (UPDATE remove_source_queue SET url=NEW.url where url=NEW.url);


CREATE TABLE resource (
  URI text NOT NULL,
  URI_HASH bigint NOT NULL,
  FIRSTSEEN_SOURCE bigint DEFAULT '0' NOT NULL,
  FIRSTSEEN_TIME bigint DEFAULT '0' NOT NULL,
  LASTMODIFIED_TIME bigint DEFAULT '0' NOT NULL
);
ALTER TABLE resource ADD PRIMARY KEY (uri_hash);
CREATE INDEX resource_firstseen_source ON resource (firstseen_source);
CREATE INDEX resource_firstseen_time ON resource (firstseen_time);
CREATE INDEX resource_lastmodified_time ON resource (lastmodified_time);

CREATE RULE replace_resource AS
    ON INSERT TO resource
    WHERE
      EXISTS(SELECT 1 FROM resource WHERE uri_hash=NEW.uri_hash)
    DO INSTEAD
       (UPDATE resource SET lastmodified_time=NEW.lastmodified_time WHERE uri_hash=NEW.uri_hash);


CREATE TABLE resource_temp (
  URI text NOT NULL,
  URI_HASH bigint NOT NULL
);



CREATE TABLE spo (
  SUBJECT bigint NOT NULL,
  PREDICATE bigint NOT NULL,
  OBJECT text NOT NULL,
  OBJECT_HASH bigint NOT NULL,
  OBJECT_DOUBLE double precision DEFAULT NULL,
  ANON_SUBJ ynboolean DEFAULT 'N' NOT NULL,
  ANON_OBJ ynboolean DEFAULT 'N' NOT NULL,
  LIT_OBJ ynboolean DEFAULT 'Y' NOT NULL,
  OBJ_LANG varchar(10) DEFAULT '' NOT NULL,
  OBJ_DERIV_SOURCE bigint DEFAULT '0' NOT NULL,
  OBJ_DERIV_SOURCE_GEN_TIME bigint DEFAULT '0' NOT NULL,
  OBJ_SOURCE_OBJECT bigint DEFAULT '0' NOT NULL,
  SOURCE bigint NOT NULL,
  GEN_TIME bigint NOT NULL
);
CREATE INDEX spo_subject ON spo (subject);
CREATE INDEX spo_predicate ON spo (predicate);
CREATE INDEX spo_object_hash ON spo (object_hash);
CREATE INDEX spo_object_double ON spo (object_double);
CREATE INDEX spo_source ON spo (source);
CREATE INDEX spo_gen_time ON spo (gen_time);
CREATE INDEX spo_obj_source_object ON spo (obj_source_object);
CREATE INDEX spo_object_idx ON spo USING gin(to_tsvector('simple', object));
CREATE INDEX spo_obj_deriv_source ON spo (obj_deriv_source);
CREATE INDEX spo_obj_deriv_source_gen_time ON spo (obj_deriv_source_gen_time);


CREATE TABLE spo_temp (
  SUBJECT bigint NOT NULL,
  PREDICATE bigint NOT NULL,
  OBJECT text NOT NULL,
  OBJECT_HASH bigint NOT NULL,
  OBJECT_DOUBLE double precision DEFAULT NULL,
  ANON_SUBJ ynboolean DEFAULT 'N' NOT NULL,
  ANON_OBJ ynboolean DEFAULT 'N' NOT NULL,
  LIT_OBJ ynboolean DEFAULT 'Y' NOT NULL,
  OBJ_LANG varchar(10) DEFAULT '' NOT NULL,
  OBJ_DERIV_SOURCE bigint DEFAULT '0' NOT NULL,
  OBJ_DERIV_SOURCE_GEN_TIME bigint DEFAULT '0' NOT NULL,
  OBJ_SOURCE_OBJECT bigint DEFAULT '0' NOT NULL
);



CREATE TABLE unfinished_harvest (
  SOURCE bigint NOT NULL,
  GEN_TIME bigint NOT NULL
);
ALTER TABLE unfinished_harvest ADD CONSTRAINT unfinished_harvest_unique UNIQUE (source, gen_time);



CREATE TABLE urgent_harvest_queue (
  URL varchar(1024) NOT NULL,
  TIMESTAMP timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
  PUSHED_CONTENT text
);
