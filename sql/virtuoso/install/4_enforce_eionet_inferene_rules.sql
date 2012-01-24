
-- -------------------------------------------------------------------------------------
-- Enforce backward inference based on http://cr.eionet.europa.eu/ontologies/contreg.rdf
-- -------------------------------------------------------------------------------------

DB.DBA.RDF_LOAD_RDFXML (file_to_string('../../../src/main/webapp/ontologies/contreg.rdf'), 'http://cr.eionet.europa.eu/ontologies/contreg.rdf', 'http://cr.eionet.europa.eu/ontologies/contreg.rdf');
rdfs_rule_set ('CRInferenceRule', 'http://cr.eionet.europa.eu/ontologies/contreg.rdf');

