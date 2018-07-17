INSERT INTO harvest_source (harvest_source_id, url_hash, url, emails, time_created)
VALUES (1, '2951453010645159546', 'http://rod.eionet.europa.eu/obligations', 'jaanus.heinlaid@gmail.com', '2008-02-28 10:34:22.0');

INSERT INTO harvest_source (harvest_source_id, url_hash, url, emails, time_created)
VALUES (2, '3757744151948657171', 'http://url1.ee', 'jaanus.heinlaid@gmail.com', '2008-02-28 10:34:22.0');

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, http_code)
VALUES (1, 1, 'pull', 'heinlja', 'finished', '2008-02-28 18:15:36.0', '2008-02-28 18:16:01.0', 22043, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, http_code)
VALUES (2, 1, 'pull', 'heinlja', 'finished', '2008-02-28 18:15:36.0', '2008-02-28 18:16:01.0', 22043, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started)
VALUES (3, 1, 'pull', 'heinlja', 'started', '2008-02-28 18:15:36.0');

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started)
VALUES (4, 1, 'pull', 'heinlja', 'started', '2008-02-28 18:15:36.0');
