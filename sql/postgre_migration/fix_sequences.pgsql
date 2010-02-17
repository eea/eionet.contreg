
SELECT setval('harvest_harvest_id_seq', (SELECT MAX(harvest_id) FROM harvest)+1);
SELECT setval('harvest_message_harvest_message_id_seq', (SELECT MAX(harvest_message_id) FROM harvest_message)+1);
SELECT setval('harvest_source_harvest_source_id_seq', (SELECT MAX(harvest_source_id) FROM harvest_source)+1);

