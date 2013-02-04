package eionet.cr.dao.readers;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.openrdf.query.BindingSet;

import eionet.cr.common.Namespace;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.PredicateDTO;
import eionet.cr.util.Hashes;
import eionet.cr.util.NamespaceUtil;
import eionet.cr.util.URLUtil;
import eionet.cr.util.YesNoBoolean;

/**
 * @deprecated
 */
@Deprecated
public class RDFExporter extends ResultSetMixedReader {

    /** */
    private static final Logger LOGGER = Logger.getLogger(RDFExporter.class);

    /** */
    private long sourceHash;
    private List<PredicateDTO> distinctPredicates = new ArrayList<PredicateDTO>();

    private HashMap<Long, String> namespacePrefixes = new HashMap<Long, String>();
    private HashMap<Long, String> namespaceUris = new HashMap<Long, String>();

    private String outputRDF = "";
    private long lastSubjectHash = 0;
    private boolean lastSubjectTagNotOpen = true; // Meaning that no need to close the previous subject.
    private OutputStream output;

    /**
     *
     * @param sourceHash
     * @param output
     * @throws DAOException
     */
    public static void export(long sourceHash, OutputStream output) throws DAOException {

        RDFExporter reader = null;
        try {
            reader = new RDFExporter(sourceHash, output);
            DAOFactory.get().getDao(HelperDAO.class).outputSourceTriples(reader);
        } finally {
            if (reader != null) {
                reader.closeOutput();
            }
        }
    }

    /**
     *
     * @param sourceHash
     * @param output
     * @throws DAOException
     */
    public RDFExporter(long sourceHash, OutputStream output) throws DAOException {

        this.sourceHash = sourceHash;
        this.output = output;

        distinctPredicates = DAOFactory.get().getDao(HelperDAO.class).readDistinctPredicates(sourceHash);
        fillNamespaceTables();
        outputHeader();
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
     */
    @Override
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {

        long subjectHash = rs.getLong("subjecthash");
        String subject = rs.getString("subject");
        long predicateHash = rs.getLong("predicatehash");
        String object = rs.getString("object");
        boolean literal = YesNoBoolean.parse(rs.getString("litobject"));

        if (subjectHash != lastSubjectHash) {
            if (!lastSubjectTagNotOpen) {
                outputString("\n</rdf:Description>");
            }
            String buf = "\n\n<rdf:Description rdf:about=\"";
            lastSubjectTagNotOpen = false;
            buf += subject + "\">";

            outputString(buf);
        }

        String predicateUri = findPredicate(predicateHash);
        String predicateLocalName = NamespaceUtil.extractLocalName(predicateUri);
        String namespaceUri = NamespaceUtil.extractNamespace(predicateUri);
        Long namespaceHash = Long.valueOf(Hashes.spoHash(namespaceUri));
        String namespacePrefix = namespacePrefixes.get(namespaceHash);

        outputString("\n\t<" + namespacePrefix + ":" + predicateLocalName);

        String escapedValue = StringEscapeUtils.escapeXml(object);
        if (!literal && URLUtil.isURL(object)) {
            outputString(" rdf:resource=\"" + escapedValue + "\"/>");
        } else {
            outputString(">");
            outputString(escapedValue);
            outputString("</" + namespacePrefix + ":" + predicateLocalName + ">");
        }

        lastSubjectHash = subjectHash;
    }

    public long getSourceHash() {
        return sourceHash;
    }

    private void outputHeader() {
        outputString("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
        outputString("<rdf:RDF" + getNamespaceDeclarations() + ">");
    }

    public void closeOutput() {
        if (!lastSubjectTagNotOpen) {
            outputString("\n</rdf:Description>");
        }
        outputString("\n\n</rdf:RDF>\n");
        IOUtils.closeQuietly(output);
    }

    /**
     *
     * @return
     */
    private String getNamespaceDeclarations() {

        StringBuffer buf = new StringBuffer();
        if (namespacePrefixes != null) {

            for (Entry<Long, String> entry : namespacePrefixes.entrySet()) {

                String namespacePrefix = entry.getValue();
                Long namespaceHash = entry.getKey();
                String namespaceUri = namespaceUris.get(namespaceHash);

                buf.append("\n   xmlns:").append(namespacePrefix).append("=\"").append(namespaceUri).append("\"");
            }
        }

        return buf.toString();
    }

    public String getOutputRDF() {
        return outputRDF;
    }

    private String findPredicate(Long predicateHash) {
        for (PredicateDTO predicate : distinctPredicates) {
            if (Hashes.spoHash(predicate.getValue()) == predicateHash) {
                return predicate.getValue();
            }
        }
        return null;
    }

    /**
     *
     * @param namespaceHash
     * @return
     */
    private String findNamespaceUri(Long namespaceHash) {

        if (namespaceHash != null && distinctPredicates != null && !distinctPredicates.isEmpty()) {

            for (PredicateDTO predicateDTO : distinctPredicates) {

                String predicateUri = predicateDTO.getValue();
                String namespaceUri = NamespaceUtil.extractNamespace(predicateUri);

                if (Hashes.spoHash(namespaceUri) == namespaceHash) {
                    return namespaceUri;
                }
            }
        }

        return null;
    }

    /**
     *
     * @param outputString
     */
    private void outputString(String outputString) {
        try {
            output.write(outputString.getBytes());
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }

    /**
     *
     */
    private void fillNamespaceTables() {

        int unknownNamespaceCounter = 0;

        if (distinctPredicates != null) {
            for (PredicateDTO predicateDTO : distinctPredicates) {

                String predicateUri = predicateDTO.getValue();
                String namespaceUri = NamespaceUtil.extractNamespace(predicateUri);
                Long namespaceHash = Long.valueOf(Hashes.spoHash(namespaceUri));

                namespaceUris.put(namespaceHash, namespaceUri);

                String knownNamespacePrefix = NamespaceUtil.getKnownNamespace(namespaceUri);
                if (knownNamespacePrefix == null || knownNamespacePrefix.isEmpty()) {

                    unknownNamespaceCounter++;
                    namespacePrefixes.put(namespaceHash, "ns" + unknownNamespaceCounter);
                } else {
                    namespacePrefixes.put(namespaceHash, knownNamespacePrefix);
                }
            }
        }

        Long rdfNamespaceHash = Long.valueOf(Hashes.spoHash(Namespace.RDF.getUri()));
        namespacePrefixes.put(rdfNamespaceHash, Namespace.RDF.getPrefix());
        namespaceUris.put(rdfNamespaceHash, Namespace.RDF.getUri());
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sesame.SPARQLResultSetReader#readRow(org.openrdf.query.BindingSet)
     */
    @Override
    public void readRow(BindingSet bindingSet) {

        // TODO Auto-generated method stub
    }
}
