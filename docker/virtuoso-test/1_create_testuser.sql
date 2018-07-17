-------------------------
--- User for unit tests
-------------------------
USER_CREATE('cr3test', 'zzz', vector ('SQL_ENABLE',1,'DAV_ENABLE',1));
USER_GRANT_ROLE('cr3test','SPARQL_SELECT',0);
USER_GRANT_ROLE('cr3test','SPARQL_UPDATE',0);

USER_CREATE('cr3rouser', 'yyy', vector ('DAV_ENABLE',1));
USER_GRANT_ROLE('cr3rouser','SPARQL_SELECT',0);

-----------------------------------
--- different DB for the test user
-----------------------------------
user_set_qualifier ('cr3test', 'CRTEST');

