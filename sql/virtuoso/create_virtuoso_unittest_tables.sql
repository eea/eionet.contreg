create table CRTEST.cr3test.documentation
(
	page_id varchar(255) NOT NULL,
	content_type varchar(100) NOT NULL default 'text/html',
	title varchar(512) default '',
	PRIMARY KEY (page_id)
);

create table CRTEST.cr3test.harvest_message
(
    harvest_message_id integer NOT NULL IDENTITY,
    harvest_id integer NOT NULL,
    type varchar(3) NOT NULL default '',
    message long varchar NOT NULL default '',
    stack_trace long varchar,
    PRIMARY KEY (harvest_message_id)
);
CREATE INDEX ut_harvest_message_harvest_id ON CRTEST.cr3test.harvest_message (harvest_id);

create table CRTEST.cr3test.harvest (
    harvest_id integer NOT NULL IDENTITY,
    harvest_source_id integer NOT NULL,
    type varchar(20) NOT NULL DEFAULT '',
    username varchar(45) DEFAULT NULL,
    status varchar(10) NOT NULL DEFAULT '',
    started datetime,
    finished datetime,
    tot_statements integer,
    lit_statements integer,
    res_statements integer,
    enc_schemes integer,
    PRIMARY KEY (harvest_id)
);

create table CRTEST.cr3test.harvest_source (
    harvest_source_id integer NOT NULL IDENTITY,
    url_hash bigint NOT NULL,
    url varchar(1024) NOT NULL,
    emails varchar(255) DEFAULT NULL,
    time_created datetime NOT NULL,
    statements integer,
    count_unavail integer NOT NULL DEFAULT 0,
    last_harvest datetime,
    interval_minutes integer NOT NULL DEFAULT 0,
    source bigint NOT NULL DEFAULT 0,
    gen_time bigint NOT NULL DEFAULT 0,
    last_harvest_failed character NOT NULL DEFAULT 'N',
    priority_source character NOT NULL DEFAULT 'N',
    source_owner varchar(20) NOT NULL DEFAULT 'harvester',
    permanent_error character NOT NULL DEFAULT 'N',
    media_type varchar(255) DEFAULT NULL,
    PRIMARY KEY (url_hash)
);

create table CRTEST.cr3test.urgent_harvest_queue
(
	url varchar(1024) NOT NULL,
	"timestamp" datetime NOT NULL,
	pushed_content long varchar
);

create table CRTEST.cr3test.spo_binary
(
	subject bigint NOT NULL,
	obj_lang varchar(10) NOT NULL DEFAULT '',
	datatype varchar(50) NOT NULL DEFAULT '',
	must_embed character NOT NULL DEFAULT 'N',
	PRIMARY KEY (subject)
);

create table CRTEST.cr3test.remove_source_queue
(
	url varchar(1024) NOT NULL,
	PRIMARY KEY (url)
);

create table CRTEST.cr3test.post_harvest_script
(
    post_harvest_script_id integer NOT NULL IDENTITY,
    target_source_url varchar(1024) DEFAULT NULL,
    target_type_url varchar(1024) DEFAULT NULL,
    title varchar(255) NOT NULL,
    script long varchar NOT NULL,
    position_number integer not null,
    active character NOT NULL DEFAULT 'N',
    PRIMARY KEY (post_harvest_script_id),
    UNIQUE (target_source_url,target_type_url,title),
    UNIQUE (target_source_url,target_type_url,position_number),
    CHECK (target_source_url is null or target_type_url is null)
);
CREATE INDEX post_harvest_script_source_url ON CRTEST.cr3test.post_harvest_script (target_source_url);
CREATE INDEX post_harvest_script_type_uri ON CRTEST.cr3test.post_harvest_script (target_type_url);