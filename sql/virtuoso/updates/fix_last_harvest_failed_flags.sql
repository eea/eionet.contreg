-- Create temporary helper table that will contain the ids of last harvests and corresponding source ids.
create table CR.cr3user.last_harvests (harvest_id integer,harvest_source_id integer);

-- Insert last harvest ids and corresponding source ids into the helper table.
insert into CR.cr3user.last_harvests select max(harvest_id),harvest_source_id from CR.cr3user.harvest group by harvest_source_id;

-- From the helper table remove all successful harvests (i.e. it now contains only last harvests that have failed).
delete from CR.cr3user.last_harvests where harvest_id not in (select harvest_id from CR.cr3user.harvest_message where type='err');

-- Fix all harvest sources whose last-harvest-failed flag is up, yet they are not present in last harvests that have failed.
update CR.cr3user.harvest_source set last_harvest_failed='N' where last_harvest_failed='Y' and harvest_source_id not in (select distinct harvest_source_id from CR.cr3user.last_harvests);

-- Drop the temporary helper table.
drop table CR.cr3user.last_harvests;
