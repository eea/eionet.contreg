
create table CR.cr3user.post_harvest_script
(
    post_harvest_script_id integer NOT NULL IDENTITY,
    harvest_source_url varchar(1024) DEFAULT NULL,
    rdf_type_uri varchar(1024) DEFAULT NULL,
    query long varchar NOT NULL,
    position_index integer not null,
    PRIMARY KEY (post_harvest_script_id),
    UNIQUE (harvest_source_url,rdf_type_uri,position_index),
    CHECK ((harvest_source_url is not null and rdf_type_uri is null) or (harvest_source_url is null and rdf_type_uri is not null))
);
CREATE INDEX post_harvest_script_source_url ON CR.cr3user.post_harvest_script (harvest_source_url);
CREATE INDEX post_harvest_script_type_uri ON CR.cr3user.post_harvest_script (rdf_type_uri);
