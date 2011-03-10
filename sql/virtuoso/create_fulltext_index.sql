-- The following script creates full text index on RDF data in Virtuoso:
-- The result is that all string valued objects will be added to a text index. Yet the index doesn't automatically update itself.

DB.DBA.RDF_OBJ_FT_RULE_ADD (null, null, 'All');	

-- To force synchronization of the RDF text index, use:
DB.DBA.VT_INC_INDEX_DB_DBA_RDF_OBJ ();

-- To set the text index to follow the triples in real time, use:
DB.DBA.VT_BATCH_UPDATE ('DB.DBA.RDF_OBJ', 'OFF', null);

-- To set the text index to be updated every 10 minutes, use:´
-- DB.DBA.VT_BATCH_UPDATE ('DB.DBA.RDF_OBJ', 'ON', 10);
