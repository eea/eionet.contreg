
ALTER TABLE harvest ADD PRIMARY KEY (harvest_id);
ALTER TABLE harvest ADD CONSTRAINT harvest_unique UNIQUE (harvest_source_id, started);

ALTER TABLE harvest_message ADD PRIMARY KEY (harvest_message_id);
CREATE INDEX harvest_message_harvest_id ON harvest_message (harvest_id);

ALTER TABLE harvest_source ADD PRIMARY KEY (harvest_source_id);
ALTER TABLE harvest_source ADD CONSTRAINT harvest_source_unique UNIQUE (url_hash);
CREATE RULE skip_duplicate_harvest_source AS
    ON INSERT TO harvest_source
    WHERE
      EXISTS(SELECT 1 FROM harvest_source WHERE url_hash=NEW.url_hash)
    DO INSTEAD (UPDATE harvest_source SET url_hash=NEW.url_hash where url_hash=NEW.url_hash);

ALTER TABLE remove_source_queue ADD PRIMARY KEY (url);
CREATE RULE skip_duplicate_remove_source AS
    ON INSERT TO remove_source_queue
    WHERE
      EXISTS(SELECT 1 FROM remove_source_queue WHERE url=NEW.url)
    DO INSTEAD (UPDATE remove_source_queue SET url=NEW.url where url=NEW.url);

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

CREATE INDEX spo_subject ON spo (subject);
CREATE INDEX spo_predicate ON spo (predicate);
CREATE INDEX spo_object_hash ON spo (object_hash);
CREATE INDEX spo_object_double ON spo (object_double);
CREATE INDEX spo_source ON spo (source);
CREATE INDEX spo_gen_time ON spo (gen_time);
CREATE INDEX spo_obj_source_object ON spo (obj_source_object);
CREATE INDEX spo_object_idx ON spo USING gin(to_tsvector('simple', object));

ALTER TABLE unfinished_harvest ADD CONSTRAINT unfinished_harvest_unique UNIQUE (source, gen_time);
