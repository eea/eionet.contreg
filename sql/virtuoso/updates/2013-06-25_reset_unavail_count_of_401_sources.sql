
--
-- This Virtuoso-specific script set the COUNT_UNAVAIL value to 0 in all HARVEST_SOURCE records that have an associated
-- HARVEST that ended up with HTTP_CODE 401 (i.e. unauthorized).
--

UPDATE CR.cr3user.harvest_source SET count_unavail=0 WHERE harvest_source_id in (select harvest_source.harvest_source_id from harvest_source, harvest where harvest_source.last_harvest_id=harvest.harvest_id and harvest.http_code=401);
