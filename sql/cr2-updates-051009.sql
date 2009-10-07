
-- PLEASE NOTE, this update locks some of the database tables to ensure the data integrity.

alter table HARVEST_SOURCE add column URL_HASH bigint(20) not null default 0 after HARVEST_SOURCE_ID;
 
-- Create temp table to store id / hash pairs
create table TEMP_UPDATE_051009 (ID bigint, HASH bigint) engine = MyISAM;
-- create index on hash column
create index hash_index on TEMP_UPDATE_051009 (ID); 
-- lock the involved tables
lock tables HARVEST_SOURCE WRITE, RESOURCE WRITE, TEMP_UPDATE_051009 WRITE;
-- populate TEMP_UPDATE_051009 table with hash
insert into TEMP_UPDATE_051009 (ID, HASH) select HARVEST_SOURCE_ID, URI_HASH from HARVEST_SOURCE inner join RESOURCE on HARVEST_SOURCE.URL = RESOURCE.URI;
-- delete harvest_source, which have duplicates (case_sensivity problem) in resource table
delete from HARVEST_SOURCE where HARVEST_SOURCE_ID in (select ID from  (select count(id) C , ID from TEMP_UPDATE_051009  group by ID having C > 1) as TEMP_UPDATE_051009);
-- update harvest_source with url_hash 
update HARVEST_SOURCE  set URL_HASH = (select HASH from TEMP_UPDATE_051009 where ID = HARVEST_SOURCE.HARVEST_SOURCE_ID);
-- drop temp table
drop table TEMP_UPDATE_051009;
unlock tables;

-- add unique key constraint to URL_HASH
alter table HARVEST_SOURCE modify column URL_HASH bigint(20) not null unique;

-- change URL
alter table HARVEST_SOURCE drop key URL;
alter table HARVEST_SOURCE modify column URL varchar(1024);


