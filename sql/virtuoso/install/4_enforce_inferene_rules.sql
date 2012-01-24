
-- -------------------------------------------------------------------------------------
-- Enforce backward inference based on http://cr.eionet.europa.eu/ontologies/contreg.rdf
-- -------------------------------------------------------------------------------------

DB.DBA.RDF_LOAD_RDFXML (http_get('http://svn.eionet.europa.eu/projects/Reportnet/browser/cr3/trunk/src/main/webapp/ontologies/contreg.rdf?format=txt'), 'http://cr.eionet.europa.eu/ontologies/contreg.rdf', 'http://cr.eionet.europa.eu/ontologies/contreg.rdf');
rdfs_rule_set ('CRInferenceRule', 'http://cr.eionet.europa.eu/ontologies/contreg.rdf');

