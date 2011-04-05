ALTER TABLE harvest_source ADD COLUMN priority_source ynboolean NOT NULL DEFAULT 'N'::ynboolean;
ALTER TABLE harvest_source ADD COLUMN source_owner character varying(20) NOT NULL DEFAULT 'harvester';

UPDATE harvest_source SET priority_source='Y' WHERE tracked_file='N';
ALTER TABLE harvest_source DROP COLUMN tracked_file;
