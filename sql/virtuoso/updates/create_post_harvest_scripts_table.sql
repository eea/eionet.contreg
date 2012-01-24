
create table CR.cr3user.post_harvest_script
(
    post_harvest_script_id integer NOT NULL IDENTITY,
    target_source_url varchar(1024) DEFAULT NULL,
    target_type_url varchar(1024) DEFAULT NULL,
    title varchar(255) NOT NULL,
    script long varchar NOT NULL,
    position_number integer not null,
    active character NOT NULL DEFAULT 'N',
    run_once character NOT NULL DEFAULT 'Y',
    PRIMARY KEY (post_harvest_script_id),
    UNIQUE (target_source_url,target_type_url,title),
    UNIQUE (target_source_url,target_type_url,position_number),
    CHECK (target_source_url is null or target_type_url is null)
);
CREATE INDEX post_harvest_script_source_url ON CR.cr3user.post_harvest_script (target_source_url);
CREATE INDEX post_harvest_script_type_uri ON CR.cr3user.post_harvest_script (target_type_url);
