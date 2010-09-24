package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringEscapeUtils;

import eionet.cr.common.Namespace;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.PredicateDTO;
import eionet.cr.util.Hashes;
import eionet.cr.util.NamespaceUtil;
import eionet.cr.util.URLUtil;
import eionet.cr.util.sql.ResultSetBaseReader;


public class RDFExporter extends ResultSetBaseReader {

	private long sourceHash;
	private List<PredicateDTO> distinctPredicates = new ArrayList<PredicateDTO>();
	private HashMap<Long,String> namespaces = new HashMap<Long, String>();
	private String outputRDF = "";
	private long lastSubjectHash = 0;
	private boolean lastSubjectDescriptionTagClosed = true; // Meaning that no need to close the previous subject.
	
	public static String export(long sourceHash){
		RDFExporter reader = null;
		try {
			reader = new RDFExporter(sourceHash);
			 
			DAOFactory.get().getDao(HelperDAO.class).outputSourceTriples(reader);
		} catch (Exception ex){
			
		}
		reader.closeOutput();
		return reader.getOutputRDF();
	}
	
	public RDFExporter(long sourceHash){
		
		NamespaceUtil.addNamespace(namespaces, Namespace.RDF);
		NamespaceUtil.addNamespace(namespaces, Namespace.RDFS);
		
		this.sourceHash = sourceHash;
		try {
			namespaces = getNamespaces();
			distinctPredicates = DAOFactory.get().getDao(HelperDAO.class).readDistinctPredicates(sourceHash);

		} catch (Exception ex){
		}
		outputHeader();
	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
	 */
	public void readRow(ResultSet rs) throws SQLException {
/*		
		TripleDTO dto = new TripleDTO(rs.getLong("SUBJECT"),
				rs.getLong("PREDICATE"), rs.getString("OBJECT"));
		dto.setObjectDerivSourceHash(rs.getLong("OBJ_DERIV_SOURCE"));
*/
		long subjectHash = rs.getLong("subjecthash");
		String subject = rs.getString("subject");
		long predicateHash = rs.getLong("predicatehash");
		String object = rs.getString("object");
		String litobject = rs.getString("litobject");
		
		System.out.println( rs.getLong("subjecthash") + " - " + rs.getString("subject") + " - "+ rs.getLong("predicatehash") + " - "+ rs.getString("object"));
		
		if (subjectHash != lastSubjectHash){
			if (!lastSubjectDescriptionTagClosed){
				outputRDF += "\n</rdf:Description>";
			}			
			StringBuffer buf = new StringBuffer("\n\n<rdf:Description rdf:about=\"");
			lastSubjectDescriptionTagClosed = false;
			buf.append(subject).append("\">");
			outputRDF += buf.toString();			
		}
		
		boolean literal = false;
		if (litobject.toLowerCase().equals("y")){
			literal = true;
		}
		
		String predicate = NamespaceUtil.extractPredicate(findPredicateValue(predicateHash));
		outputRDF += "\n\t\t<" + namespaces.get(predicateHash) + ":" + predicate;

		String escapedValue = StringEscapeUtils.escapeXml(object);
		if (!literal && URLUtil.isURL(object)){
			outputRDF += " rdf:resource=\"" + escapedValue + "\"/>";
		}
		else{
			outputRDF += ">";
			outputRDF += escapedValue;
			outputRDF += "</" + namespaces.get(predicateHash) + ":"  + predicate + ">";
		}
		
		
		lastSubjectHash = subjectHash;
		// remember distinct hashes encountered
	}
	
	private HashMap<Long, String> getNamespaces(){
		try {
			return NamespaceUtil.getNamespacePrefix(distinctPredicates);
		} catch (DAOException ex){
			return null;
		}
	}

	public long getSourceHash() {
		return sourceHash;
	}
	
	private void outputHeader(){
		outputRDF = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n";
		outputRDF += ("<rdf:RDF" + getNamespaceDeclarations() + ">");
	}
	
	public void closeOutput(){
		if (!lastSubjectDescriptionTagClosed){
			outputRDF += "\n</rdf:Description>";
		}	
		outputRDF += "\n\n</rdf:RDF>\n";
	}
	
	private String getNamespaceDeclarations(){
		StringBuffer namespaceDeclaration = new StringBuffer();
		if (!namespaces.isEmpty()){
			for (Entry<Long, String> entry : namespaces.entrySet()){
				namespaceDeclaration.append("\n   xmlns:").append(entry.getValue()).append("=\"").append(findPredicateNamespace(entry.getKey())).append("\"");
			}
		}
		return namespaceDeclaration.toString();
	}


	
	public String getOutputRDF() {
		return outputRDF;
	}
	
	private String findPredicateValue(Long predicateHash){
		for (PredicateDTO predicate:distinctPredicates){
			
			if (Hashes.spoHash(NamespaceUtil.extractNamespace(predicate.getValue())) == predicateHash){
				return NamespaceUtil.extractPredicate(predicate.getValue());
			}
		}
		return null;
	}
	
	private String findPredicateNamespace(Long predicateHash){
		for (PredicateDTO predicate:distinctPredicates){
			if (Hashes.spoHash(NamespaceUtil.extractNamespace(predicate.getValue())) == predicateHash){
				return NamespaceUtil.extractNamespace(predicate.getValue());
			}
		}
		return null;
	}
}
