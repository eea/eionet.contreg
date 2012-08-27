package eionet.cr.dao.virtuoso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;

import eionet.cr.common.Predicates;
import eionet.cr.dao.CompiledDatasetDAO;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.readers.DeliveryFilesReader;
import eionet.cr.dao.readers.ResultSetReaderException;
import eionet.cr.dto.DatasetDTO;
import eionet.cr.dto.DeliveryFilesDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Bindings;
import eionet.cr.util.Util;
import eionet.cr.util.sesame.SPARQLQueryUtil;
import eionet.cr.util.sesame.SPARQLResultSetBaseReader;
import eionet.cr.util.sesame.SPARQLResultSetReader;
import eionet.cr.util.sesame.SesameConnectionProvider;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SingleObjectReader;

/**
 * DAO methods for compiled datasets in Virtuoso.
 *
 * @author altnyris
 */
public class VirtuosoCompiledDatasetDAO extends VirtuosoBaseDAO implements CompiledDatasetDAO {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DeliveryFilesDTO> getDeliveryFiles(List<String> deliveryUris) throws DAOException {
        List<DeliveryFilesDTO> ret = new ArrayList<DeliveryFilesDTO>();
        if (deliveryUris != null && deliveryUris.size() > 0) {

            StringBuffer query = new StringBuffer();
            query.append("select distinct ?s ?o ?triplesCnt ?title where {");
            query.append("?s <").append(Predicates.ROD_HAS_FILE).append("> ?o . ");
            query.append("?o <").append(Predicates.CR_MEDIA_TYPE).append("> \"text/xml\" . ");
            query.append("filter(?s IN (").append(SPARQLQueryUtil.urisToCSV(deliveryUris)).append("))");
            query.append("OPTIONAL {?o <").append(Predicates.CR_HARVESTED_STATEMENTS).append("> ?triplesCnt } ");
            query.append("OPTIONAL {?s <").append(Predicates.DC_TITLE).append("> ?title } ");
            query.append("} ORDER BY ?s");

            /*
             * StringBuffer query = new StringBuffer(); query.append("select ?s ?o ?title count(?s1) ?triplesCnt where {");
             * query.append("?s <").append(Predicates.ROD_HAS_FILE).append("> ?o . ");
             * query.append("filter(?s IN (").append(SPARQLQueryUtil.urisToCSV(deliveryUris)).append("))");
             * query.append("OPTIONAL {?o <").append(Predicates.CR_HARVESTED_STATEMENTS).append("> ?triplesCnt}");
             * query.append("OPTIONAL {"); query.append("?o ?p ?title .");
             * query.append("filter (?p IN (<").append(Predicates.DC_TITLE).append(">,");
             * query.append("<").append(Predicates.DCTERMS_TITLE).append(">,");
             * query.append("<").append(Predicates.RDFS_LABEL).append(">))"); query.append("}"); query.append("graph ?o {");
             * query.append("?s1 ?p1 ?o1"); query.append("}} ORDER BY ?s");
             */

            ret = executeSPARQL(query.toString(), new DeliveryFilesReader());
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DatasetDTO> getCompiledDatasets(String homeFolder, String excludeFileUri) throws DAOException {
        List<DatasetDTO> ret = new ArrayList<DatasetDTO>();
        if (!StringUtils.isBlank(homeFolder)) {
            StringBuffer query = new StringBuffer();
            query.append("select distinct ?value ?name ?modified where {");
            query.append("graph ?g { ");
            query.append("?value ?p <").append(Predicates.CR_COMPILED_DATASET).append("> .");
            query.append("filter (?g = <").append(homeFolder).append(">) .");
            if (!StringUtils.isBlank(excludeFileUri)) {

                query.append("filter (!bif:exists ( (select (1) where {");
                query.append("?value <").append(Predicates.CR_GENERATED_FROM).append("> ?o .");
                query.append("filter (?o = <").append(excludeFileUri).append(">)");
                query.append("} ) ) )");

            }
            query.append("?value <http://www.w3.org/2000/01/rdf-schema#label> ?name }");
            query.append("?value <").append(Predicates.CR_LAST_MODIFIED).append("> ?modified");
            query.append("} ORDER BY ?value");

            ret = executeSPARQL(query.toString(), new SPARQLResultSetReader<DatasetDTO>() {
                List<DatasetDTO> result = new ArrayList<DatasetDTO>();

                @Override
                public List<DatasetDTO> getResultList() {
                    return result;
                }

                @Override
                public void endResultSet() {
                }

                @Override
                public void startResultSet(List<String> bindingNames) {
                }

                @Override
                public void readRow(BindingSet bindingSet) throws ResultSetReaderException {
                    DatasetDTO item = new DatasetDTO();
                    item.setUri(bindingSet.getValue("value").stringValue());
                    item.setLabel(bindingSet.getValue("name").stringValue());
                    item.setModified(Util.virtuosoStringToDate(bindingSet.getValue("modified").stringValue()));
                    result.add(item);
                }

            });
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getDatasetFiles(String dataset) throws DAOException {
        List<String> ret = new ArrayList<String>();
        if (!StringUtils.isBlank(dataset)) {
            StringBuffer query = new StringBuffer();
            query.append("select distinct(?o) where {");
            query.append("<").append(dataset).append("> <").append(Predicates.CR_GENERATED_FROM).append("> ?o");
            query.append("}");

            ret = executeSPARQL(query.toString(), new SingleObjectReader<String>());
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SubjectDTO> getDetailedDatasetFiles(String dataset) throws DAOException {
        List<SubjectDTO> ret = new ArrayList<SubjectDTO>();
        if (!StringUtils.isBlank(dataset)) {
            StringBuffer query = new StringBuffer();
            query.append("select ?source, ?lastModified where {");
            query.append("<").append(dataset).append("> <").append(Predicates.CR_GENERATED_FROM).append("> ?source . ");
            query.append("OPTIONAL {?source").append("<" + Predicates.CR_LAST_MODIFIED + ">").append(" ?lastModified}");
            query.append("}");

            SPARQLResultSetBaseReader<SubjectDTO> reader = new SPARQLResultSetBaseReader<SubjectDTO>() {
                @Override
                public void readRow(BindingSet bindingSet) throws ResultSetReaderException {
                    if (bindingSet != null && bindingSet.size() > 0) {
                        String sourceUri = bindingSet.getValue("source").stringValue();
                        Value lastModifiedDate = bindingSet.getValue("lastModified");
                        Date lastModified = null;
                        if (lastModifiedDate != null) {
                            lastModified = Util.virtuosoStringToDate(lastModifiedDate.stringValue());
                        }

                        SubjectDTO dto = new SubjectDTO(sourceUri, false);
                        dto.setLastModifiedDate(lastModified);
                        resultList.add(dto);
                    }
                }
            };

            ret = executeSPARQL(query.toString(), reader);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveDataset(List<String> selectedFiles, String datasetUri, boolean overwrite) throws DAOException {

        RepositoryConnection con = null;
        try {
            con = SesameConnectionProvider.getRepositoryConnection();

            StringBuffer query = new StringBuffer();
            query.append("INSERT INTO GRAPH ?graphUri { ?s ?p ?o } ");
            query.append("WHERE {graph ?g { ?s ?p ?o . ");
            query.append("filter (?g IN (").append(SPARQLQueryUtil.urisToCSV(selectedFiles)).append("))");
            query.append("}}");

            Bindings bindings = new Bindings();
            bindings.setURI("graphUri", datasetUri);
            executeSPARUL(query.toString(), bindings, con);

        } catch (Exception e) {
            e.printStackTrace();
            throw new DAOException(e.getMessage(), e);
        } finally {
            SesameUtil.close(con);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearDataset(String datasetUri, String contextUri) throws DAOException {

        RepositoryConnection con = null;
        try {
            con = SesameConnectionProvider.getRepositoryConnection();

            con.clear(con.getValueFactory().createURI(datasetUri));

            URI context = con.getValueFactory().createURI(contextUri);
            URI sub = con.getValueFactory().createURI(datasetUri);
            con.remove(sub, null, null, context);

        } catch (Exception e) {
            e.printStackTrace();
            throw new DAOException(e.getMessage(), e);
        } finally {
            SesameUtil.close(con);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearDatasetData(String datasetUri) throws DAOException {
        RepositoryConnection con = null;
        try {
            con = SesameConnectionProvider.getRepositoryConnection();

            con.clear(con.getValueFactory().createURI(datasetUri));
        } catch (Exception e) {
            e.printStackTrace();
            throw new DAOException(e.getMessage(), e);
        } finally {
            SesameUtil.close(con);
        }
    }

    /**
     * SPARQL for detecting if dataset exists.
     */
    private static final String DATASET_EXISTS_QUERY = "ASK {?graphUri a ?datasetType}";

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean datasetExists(String uri) throws DAOException {

        boolean ret = false;

        if (uri == null) {
            throw new IllegalArgumentException("Dataset URI must not be null");
        }

        RepositoryConnection con = null;

        try {
            con = SesameConnectionProvider.getReadOnlyRepositoryConnection();

            Bindings bindings = new Bindings();
            bindings.setURI("graphUri", uri);
            bindings.setURI("datasetType", Predicates.CR_COMPILED_DATASET);

            BooleanQuery booleanQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL, DATASET_EXISTS_QUERY);
            bindings.applyTo(booleanQuery, con.getValueFactory());
            Boolean result = booleanQuery.evaluate();
            if (result != null) {
                ret = result.booleanValue();
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new DAOException(e.getMessage(), e);
        } finally {
            SesameUtil.close(con);
        }

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeFiles(String datasetUri, List<String> selectedFiles) throws DAOException {

        if (StringUtils.isEmpty(datasetUri)) {
            throw new IllegalArgumentException("Dataset URI must not be null");
        }

        if (selectedFiles == null || selectedFiles.size() == 0) {
            return;
        }

        RepositoryConnection con = null;
        try {
            con = SesameConnectionProvider.getRepositoryConnection();
            for (String graph : selectedFiles) {
                StringBuffer query = new StringBuffer();
                query.append("DELETE FROM ?graphUri { ?s ?p ?o } ");
                query.append("WHERE { GRAPH ?originalGraphUri { ?s ?p ?o }}");

                Bindings bindings = new Bindings();
                bindings.setURI("graphUri", datasetUri);
                bindings.setURI("originalGraphUri", graph);
                executeSPARUL(query.toString(), bindings, con);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DAOException(e.getMessage(), e);
        } finally {
            SesameUtil.close(con);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasCompiledDatasetExpiredData(String datasetUri, List<String> selectedFiles) throws DAOException {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ?time WHERE {?dataset ?latModified ?time} ");
        Bindings bindings = new Bindings();
        bindings.setURI("dataset", datasetUri);
        bindings.setURI("latModified", Predicates.CR_LAST_MODIFIED);

        String datasetLastModified = executeUniqueResultSPARQL(sb.toString(), bindings, new SingleObjectReader<String>());

        sb = new StringBuilder();
        sb.append("SELECT xsd:dateTime(?datasetLastModified) > ?time WHERE {?dataset ?lastModified ?time}");

        for (String file : selectedFiles) {
            bindings = new Bindings();
            bindings.setString("datasetLastModified", datasetLastModified);
            bindings.setURI("dataset", file);
            bindings.setURI("lastModified", Predicates.CR_LAST_MODIFIED);

            String result = executeUniqueResultSPARQL(sb.toString(), bindings, new SingleObjectReader<String>());
            if (!StringUtils.isBlank(result)) {
                int resultInt = Integer.parseInt(result);
                if (resultInt != 1) {
                    return true;
                }
            }
        }

        return false;
    }

}
