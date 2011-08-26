-- No need to limit the length of harvest source URL
alter table HARVEST_SOURCE alter column URL type text;
alter table URGENT_HARVEST_QUEUE alter column URL type text;

-- Before the URL column in REMOVE_SOURCE_QUEUE can be altered, the skip_duplicate_remove_source rule has to be removed. And then re-created later.
drop rule skip_duplicate_remove_source on remove_source_queue;
alter table REMOVE_SOURCE_QUEUE alter column URL type text;
CREATE RULE skip_duplicate_remove_source AS ON INSERT TO remove_source_queue WHERE (EXISTS (SELECT 1 FROM remove_source_queue WHERE ((remove_source_queue.url)::text = (new.url)::text))) DO INSTEAD UPDATE remove_source_queue SET url = new.url WHERE ((remove_source_queue.url)::text = (new.url)::text);



