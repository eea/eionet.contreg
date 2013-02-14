create table CR.cr3user.endpoint_harvest_query
(
    endpoint_harvest_query_id integer NOT NULL IDENTITY,
    title varchar(255) NOT NULL,
    query long varchar NOT NULL,
    endpoint_url varchar(1024) NOT NULL,
    endpoint_url_hash bigint NOT NULL,
    position_number integer not null,
    active character NOT NULL DEFAULT 'N',
    PRIMARY KEY (endpoint_harvest_query_id),
    UNIQUE (endpoint_url_hash, title),
    UNIQUE (endpoint_url_hash, position_number),
    CONSTRAINT fk_url_hash FOREIGN KEY (endpoint_url_hash) REFERENCES CR.cr3user.harvest_source (url_hash) ON UPDATE CASCADE ON DELETE CASCADE
);

alter table CR.cr3user.harvest_source add column is_sparql_endpoint character NOT NULL DEFAULT 'N';
