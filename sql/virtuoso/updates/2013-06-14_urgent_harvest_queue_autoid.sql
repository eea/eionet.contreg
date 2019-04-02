
--
-- This Virtuoso-specific script adds a new auto-id column named "item_id" to the CR.cr3user.urgent_harvest_queue_new table,
-- makes it the first column in the table and populates it with values.   
--

create table CR.cr3user.urgent_harvest_queue_new
(
    item_id integer NOT NULL IDENTITY,
    url varchar(1024) NOT NULL,
    "timestamp" datetime NOT NULL,
    pushed_content long varchar,
    username varchar(45) DEFAULT NULL,
    PRIMARY KEY (item_id)
);

insert into CR.cr3user.urgent_harvest_queue_new (url, "timestamp", pushed_content)
        select url, "timestamp", pushed_content from CR.cr3user.urgent_harvest_queue order by "timestamp" asc;

drop table CR.cr3user.urgent_harvest_queue;
alter table CR.cr3user.urgent_harvest_queue_new RENAME CR.cr3user.urgent_harvest_queue;

