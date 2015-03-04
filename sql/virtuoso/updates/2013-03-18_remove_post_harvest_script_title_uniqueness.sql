
CREATE TABLE CR.cr3user.post_harvest_script_new
(
    post_harvest_script_id integer NOT NULL IDENTITY,
    target_source_url varchar(1024) DEFAULT NULL,
    target_type_url varchar(1024) DEFAULT NULL,
    title varchar(255) NOT NULL,
    script long varchar NOT NULL,
    position_number integer not null,
    active character NOT NULL DEFAULT 'N',
    run_once character NOT NULL DEFAULT 'Y',
    last_modified datetime,
    PRIMARY KEY (post_harvest_script_id),
    UNIQUE (target_source_url,target_type_url,position_number),
    CHECK (target_source_url is null or target_type_url is null)
);

INSERT INTO CR.cr3user.post_harvest_script_new
    (post_harvest_script_id,target_source_url,target_type_url,title,script,position_number,active,run_once,last_modified)
    SELECT post_harvest_script_id,target_source_url,target_type_url,title,script,position_number,active,run_once,last_modified
    FROM CR.cr3user.post_harvest_script;

DROP TABLE CR.cr3user.post_harvest_script;

ALTER TABLE CR.cr3user.post_harvest_script_new RENAME CR.cr3user.post_harvest_script;
CREATE INDEX post_harvest_script_source_url ON CR.cr3user.post_harvest_script (target_source_url);
CREATE INDEX post_harvest_script_type_uri ON CR.cr3user.post_harvest_script (target_type_url);

