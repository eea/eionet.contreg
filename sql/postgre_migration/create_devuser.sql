CREATE ROLE cr2dev LOGIN PASSWORD 'your_password';
GRANT CREATE,CONNECT,TEMPORARY,TEMP ON DATABASE cr2 TO cr2dev;
GRANT SELECT,INSERT,UPDATE,DELETE ON harvest, harvest_message, harvest_message, harvest_source, remove_source_queue, resource, resource_temp, spo, spo_temp, unfinished_harvest, urgent_harvest_queue TO cr2dev;
GRANT ALL ON SEQUENCE harvest_message_harvest_message_id_seq, harvest_source_harvest_source_id_seq, harvest_harvest_id_seq TO cr2dev;
