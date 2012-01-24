create table CR.cr3user.documentation
(
	page_id varchar(255) NOT NULL,
	content_type varchar(100) NOT NULL default 'text/html',
	title varchar(512) default '',
	PRIMARY KEY (page_id)
);

create table CR.cr3user.harvest_message
(
    harvest_message_id integer NOT NULL IDENTITY,
    harvest_id integer NOT NULL,
    type varchar(3) NOT NULL default '',
    message long varchar NOT NULL default '',
    stack_trace long varchar,
    PRIMARY KEY (harvest_message_id)
);
CREATE INDEX harvest_message_harvest_id ON CR.cr3user.harvest_message (harvest_id);

create table CR.cr3user.harvest (
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

create table CR.cr3user.harvest_source (
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

create table CR.cr3user.urgent_harvest_queue
(
	url varchar(1024) NOT NULL,
	"timestamp" datetime NOT NULL,
	pushed_content long varchar
);

create table CR.cr3user.spo_binary
(
	subject bigint NOT NULL,
	obj_lang varchar(10) NOT NULL DEFAULT '',
	datatype varchar(50) NOT NULL DEFAULT '',
	must_embed character NOT NULL DEFAULT 'N',
	PRIMARY KEY (subject)
);

create table CR.cr3user.remove_source_queue
(
	url varchar(1024) NOT NULL,
	PRIMARY KEY (url)
);

create table CR.cr3user.post_harvest_script
(
  post_harvest_script_id INTEGER IDENTITY,
  target_source_url VARCHAR(1024),
  target_type_url VARCHAR(1024),
  title VARCHAR(255),
  script LONG VARCHAR,
  position_number INTEGER,
  active VARCHAR(1),
  PRIMARY KEY (post_harvest_script_id)
);

ALTER TABLE CR.cr3user.post_harvest_script
  ADD CHECK ( target_source_url  IS NULL OR  target_type_url  IS NULL);

create procedure CR.cr3user.period_end_year (in txt varchar) {
        declare start_year integer;
        declare end_year integer;
        declare period integer;
        declare pidx integer;
        declare yidx integer;
        declare ylength integer;

        pidx := strcasestr(txt, '/P');
        yidx := strcasestr(txt, 'Y');
        if (pidx > 0 and yidx > 0) {
                ylength := yidx - pidx - 2;
                start_year := cast(substring(txt, 1 , 4) as integer);
                period := cast(substring(txt, pidx + 3, ylength) as integer);
                end_year := start_year + period;
        }
        return end_year;
};

ALTER TABLE CR.cr3user.harvest_message
ADD CONSTRAINT hame_ha_fk FOREIGN KEY (harvest_id) REFERENCES CR.cr3user.harvest (harvest_id)
    ON DELETE CASCADE;

ALTER TABLE CR.cr3user.harvest
ADD CONSTRAINT ha_haso_fk FOREIGN KEY (harvest_source_id) REFERENCES CR.cr3user.harvest_source (harvest_source_id)
    ON DELETE CASCADE;
