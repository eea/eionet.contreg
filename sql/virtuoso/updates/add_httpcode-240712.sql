--- HTTP code to harvest table
alter table CR.cr3user.harvest add column http_code integer;

-- update source not modified harvests with correct http code based on harvest message:
UPDATE  CR.cr3user.harvest SET http_code = 304 WHERE harvest_id IN (SELECT harvest_id FROM CR.cr3user.harvest_message where left(message,38) = 'Source not modified since last harvest');




