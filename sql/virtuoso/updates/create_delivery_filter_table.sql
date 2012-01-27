create table CR.cr3user.delivery_filter
(
    delivery_filter_id INTEGER IDENTITY,
    obligation varchar(255),
    obligation_label varchar(255),
    locality varchar(255),
    locality_label varchar(255),
    year varchar(10),
    username varchar(10) NOT NULL
);
