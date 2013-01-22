create table CR.cr3user.staging_db
(
    database_id integer NOT NULL IDENTITY,
    name varchar(150) NOT NULL,
    creator varchar(80) NOT NULL,
    created datetime NOT NULL,
    description long varchar DEFAULT NULL,
    import_status varchar(30) DEFAULT NULL,
    import_log long varchar DEFAULT NULL,
    PRIMARY KEY (database_id),
    UNIQUE (name)
);

