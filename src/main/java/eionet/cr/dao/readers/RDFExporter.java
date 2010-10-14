package eionet.cr.dao.readers;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringEscapeUtils;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.PredicateDTO;
import eionet.cr.util.Hashes;
import eionet.cr.util.NamespaceUtil;
import eionet.cr.util.URLUtil;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.sql.ResultSetBaseReader;

/**
 * 
 */
public class RDFExporter extends ResultSetBaseReader {

	/** */
	private long sourceHash;
	private List<PredicateDTO> distinctPredicates = new ArrayList<PredicateDTO>();
	private HashMap<Long,String> namespaces = new HashMap<Long, String>();
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
	public static void export(long sourceHash, OutputStream output) throws DAOException{
		
		RDFExporter reader = null;
		try {
			reader = new RDFExporter(sourceHash, output);
			DAOFactory.get().getDao(HelperDAO.class).outputSourceTriples(reader);
		}
		finally{
			if (reader!=null){
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
	public RDFExporter(long sourceHash, OutputStream output) throws DAOException{
		
		this.sourceHash = sourceHash;
		this.output = output;
		
		distinctPredicates = DAOFactory.get().getDao(HelperDAO.class).readDistinctPredicates(sourceHash);
		namespaces = getNamespaces();
		outputHeader();
	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
	 */
	public void readRow(ResultSet rs) throws SQLException {

		long subjectHash = rs.getLong("subjecthash");
		String subject = rs.getString("subject");
		long predicateHash = rs.getLong("predicatehash");
		String object = rs.getString("object");
		boolean literal = YesNoBoolean.parse(rs.getString("litobject"));
		
		if (subjectHash != lastSubjectHash){
			if (!lastSubjectTagNotOpen){
				outputString( "\n</rdf:Description>");
			}			
			String buf = "\n\n<rdf:Description rdf:about=\"";
			lastSubjectTagNotOpen = false;
			buf += subject + "\">";
			

			outputString(buf);
		}
		
		String predicate = NamespaceUtil.extractPredicate(findPredicate(predicateHash));
		
		String namespace = namespaces.get(Hashes.spoHash(NamespaceUtil.extractNamespace(findPredicate(predicateHash))));
		outputString("\n\t<" + namespace + ":" + predicate);

		String escapedValue = StringEscapeUtils.escapeXml(object);
		if (!literal && URLUtil.isURL(object)){
			outputString( " rdf:resource=\"" + escapedValue + "\"/>");
		}
		else{
			outputString(">");
			outputString(escapedValue);
			outputString("</" + namespace + ":"  + predicate + ">");
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
		outputString("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
		outputString("<rdf:RDF" + getNamespaceDeclarations() + ">");
	}
	
	public void closeOutput(){
		if (!lastSubjectTagNotOpen){
			outputString("\n</rdf:Description>");
		}	
		outputString("\n\n</rdf:RDF>\n");
		try {
			output.close();
		} catch (Exception ex){
			System.out.println(ex.getMessage());
		}
	}
	
	private String getNamespaceDeclarations(){
		StringBuffer namespaceDeclaration = new StringBuffer();
		if (!namespaces.isEmpty()){
			for (Entry<Long, String> entry : namespaces.entrySet()){
				namespaceDeclaration.append("\n   xmlns:").append(entry.getValue()).append("=\"").append(findNameSpace(entry.getKey())).append("\"");
			}
		}
		return namespaceDeclaration.toString();
	}


	
	public String getOutputRDF() {
		return outputRDF;
	}
	
	private String findPredicate(Long predicateHash){
		for (PredicateDTO predicate:distinctPredicates){
			if (Hashes.spoHash(predicate.getValue()) == predicateHash){
				return predicate.getValue();
			}
		}
		return null;
	}
	
	private String findNameSpace(Long namespaceHash){
		for (PredicateDTO predicate:distinctPredicates){
			if (Hashes.spoHash( NamespaceUtil.extractNamespace(predicate.getValue())) == namespaceHash){
				return NamespaceUtil.extractNamespace(predicate.getValue());
			}
		}
		return null;
	}
	
	private void outputString(String outputString){
		try {
			output.write(outputString.getBytes());
		} catch (IOException ex){
			
		}
	}
	
}
