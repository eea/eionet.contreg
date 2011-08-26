GRANT CREATE, CONNECT, TEMPORARY, TEMP
     ON DATABASE cr3
     TO cr3user;

GRANT SELECT, INSERT, UPDATE, DELETE
     ON harvest, harvest_message, harvest_message, harvest_source,
        remove_source_queue, resource, resource_temp,
        spo, spo_temp, unfinished_harvest, urgent_harvest_queue, spo_binary, documentation
     TO cr3user;
GRANT ALL
     ON SEQUENCE harvest_message_harvest_message_id_seq,
                 harvest_source_harvest_source_id_seq,
                 harvest_harvest_id_seq
     TO cr3user;
