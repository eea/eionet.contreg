-- ---------------------------------------------------------------------
-- Create database users for CR3 (one for read-write, one for read only)
-- ---------------------------------------------------------------------

USER_CREATE('cr3user', 'xxx', vector ('SQL_ENABLE',1,'DAV_ENABLE',1));
USER_GRANT_ROLE('cr3user','SPARQL_SELECT',0);
USER_GRANT_ROLE('cr3user','SPARQL_UPDATE',0);

GRANT SELECT ON sys_rdf_schema TO cr3user;
GRANT execute ON rdfs_rule_set TO cr3user;

USER_CREATE('cr3rouser', 'yyy', vector ('DAV_ENABLE',1));
USER_GRANT_ROLE('cr3rouser','SPARQL_SELECT',0);

-- ---------------------------------------------------------------------------
-- Set default database (or 'qualifier' as known in Virtuoso terms) of CR users
-- ---------------------------------------------------------------------------

user_set_qualifier ('cr3user', 'CR');
user_set_qualifier ('cr3rouser', 'CR');
