-- A script for creating CR user account in Virtuoso

USER_CREATE('cr3user', 'xxx', vector ('SQL_ENABLE',1,'DAV_ENABLE',1));
USER_GRANT_ROLE('cr3user','SPARQL_SELECT',0);
USER_GRANT_ROLE('cr3user','SPARQL_UPDATE',0);