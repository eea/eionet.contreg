
-- The RESOURCES column is not used any more
alter table HARVEST_SOURCE drop column RESOURCES;

-- Get rid of STATEMENT null values
update HARVEST_SOURCE set STATEMENTS=0 where STATEMENTS is null;

-- Set not-null limitation to STATEMENT, set its default value to 0
alter table HARVEST_SOURCE alter column STATEMENTS set not null;
alter table HARVEST_SOURCE alter column STATEMENTS set default 0;
