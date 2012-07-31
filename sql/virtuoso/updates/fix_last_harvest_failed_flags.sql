-- Create temporary helper table that will contain the ids of last harvests and corresponding source ids.
create table last_harvests (harvest_id integer,harvest_source_id integer);

-- Insert last harvest ids and corresponding source ids into the helper table.
insert into last_harvests select max(harvest_id),harvest_source_id from harvest group by harvest_source_id;

-- From the helper table remove all successful harvests (i.e. it now contains only last harvests that have failed).
delete from last_harvests where harvest_id not in (select harvest_id from harvest_message where type='err');

-- Fix all harvest sources whose last-harvest-failed flag is up, yet they are not present in last harvests that have failed.
update harvest_source set last_harvest_failed='N' where last_harvest_failed='Y' and harvest_source_id not in (select distinct harvest_source_id from last_harvests);

-- Drop the temporary helper table.
drop table last_harvests;
