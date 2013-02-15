alter table CR.cr3user.endpoint_harvest_query add column last_modified datetime NOT NULL;
update CR.cr3user.endpoint_harvest_query set last_modified=now();

