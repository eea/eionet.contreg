
-- drop table CR.cr3user.staging_db;

create table CR.cr3user.staging_db
(
    database_id integer NOT NULL IDENTITY,
    name varchar(150) NOT NULL,
    creator varchar(80) NOT NULL,
    created datetime NOT NULL,
    description long varchar DEFAULT NULL,
    import_status varchar(30) DEFAULT NULL,
    import_log long varchar DEFAULT NULL,
    default_query long varchar DEFAULT NULL,
    PRIMARY KEY (database_id),
    UNIQUE (name)
);

-- drop table CR.cr3user.staging_db_rdf_export;

create table CR.cr3user.staging_db_rdf_export
(
    export_id integer NOT NULL IDENTITY,
    database_id integer NOT NULL,
    export_name varchar(150) NOT NULL,
    user_name varchar(80) NOT NULL,
    query_conf long varchar NOT NULL,
    started datetime NOT NULL,
    finished datetime DEFAULT NULL,
    status varchar(30) NOT NULL,
    export_log long varchar DEFAULT NULL,
    noof_subjects integer DEFAULT NULL,
    noof_triples integer DEFAULT NULL,
    graphs long varchar DEFAULT NULL,
    UNIQUE (database_id, user_name, started),
    UNIQUE (database_id, export_name),
    PRIMARY KEY (export_id)
);
