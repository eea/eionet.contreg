package eionet.cr.harvest;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Hits;

import com.hp.hpl.jena.rdf.arp.ALiteral;
import com.hp.hpl.jena.rdf.arp.AResource;
import com.hp.hpl.jena.rdf.arp.StatementHandler;

import eionet.cr.lucene.Searcher;
import eionet.cr.util.Util;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class RDFSourceHandler implements StatementHandler{

	/** */
	private static Logger logger = Logger.getLogger(RDFSourceHandler.class);
	
	/** */
	private IndexWriter allIndexWriter;
	private String currentDocumentID;
	private ArrayList currentDocumentAttributes;
	private int countDocumentsIndexed = 0;
	
	/** */
	private String currentAnonymousID = "";
	private String currentGeneratedID = "";
	private URL sourceURL = null;
	private Searcher encSearcher = null;
	
	/** */
	private ServletContext servletContext = null;
	
	/**
	 * 
	 */
	public RDFSourceHandler(IndexWriter allIndexWriter, URL sourceURL){
		if (allIndexWriter==null)
			throw new RuntimeException("The given IndexWriter is null");
		this.allIndexWriter= allIndexWriter;
		this.sourceURL = sourceURL;
		reset();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see com.hp.hpl.jena.rdf.arp.StatementHandler#statement(com.hp.hpl.jena.rdf.arp.AResource, com.hp.hpl.jena.rdf.arp.AResource, com.hp.hpl.jena.rdf.arp.AResource)
	 */
	public void statement(AResource subject, AResource predicate, AResource object) {
		
		if (object==null)
			return;
		
		statement(subject, predicate, new LiteralMock(object, getResourceID(object)));
	}

	/*
	 *  (non-Javadoc)
	 * @see com.hp.hpl.jena.rdf.arp.StatementHandler#statement(com.hp.hpl.jena.rdf.arp.AResource, com.hp.hpl.jena.rdf.arp.AResource, com.hp.hpl.jena.rdf.arp.ALiteral)
	 */
	public void statement(AResource subject, AResource predicate, ALiteral object) {

		if (object==null)
			return;
		
		String subjectID = getResourceID(subject);
		String predicateID = getResourceID(predicate);
		if (subjectID==null || predicateID==null)
			return;
		
		if (currentDocumentID!=null && !subjectID.equals(currentDocumentID)){
			try{
				indexCurrentDocument();
			}
			catch (Exception e){
				handleError(e);
			}
			finally{
				currentDocumentAttributes = new ArrayList();
			}
		}
		
		currentDocumentID = subjectID;
		DocumentAttribute docAttr = new DocumentAttribute(predicateID, object.toString());
		docAttr.isLiteral = object instanceof LiteralMock ? false : true;
		docAttr.isAnonymous = object instanceof LiteralMock ? ((LiteralMock)object).getResource().isAnonymous() : false;
		currentDocumentAttributes.add(docAttr);
	}

	/**
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 * @throws ParseException 
	 * 
	 *
	 */
	private void indexCurrentDocument() throws CorruptIndexException, IOException, ParseException {
		
		if (currentDocumentAttributes==null || currentDocumentAttributes.size()==0){
			System.out.println("No attributes to index for " + currentDocumentID);
			return;
		}
		
		boolean hasRdfType = false;
		boolean hasRdfsLabel = false;
		StringBuffer contentBuf = new StringBuffer();
		Document document = new Document();
		
		// create the ID field, add it to the document
		document.add(new Field("ID", currentDocumentID, Field.Store.YES, Field.Index.TOKENIZED));
		
		// loop over document attributes
		for (int i=0; i<currentDocumentAttributes.size(); i++){
			
			DocumentAttribute docAttr = (DocumentAttribute)currentDocumentAttributes.get(i);
			
			// set flags, if this document has rdf-type and rdfs-label
			if (docAttr.name.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
				hasRdfType = true;
			if (docAttr.name.equals("http://www.w3.org/2000/01/rdf-schema#label"))
				hasRdfsLabel = true;
			
			// add this field to the document, add its value also to the content buffer
			document.add(new Field(docAttr.name, docAttr.value, Field.Store.YES, Field.Index.TOKENIZED));
			contentBuf.append(docAttr.value);
			
			// if this is a non-literal attribute, find decoded labels for it and them to the document
			if (!docAttr.isLiteral && docAttr.isValueURL()){
				String[] decodedLabels = EncodingSchemes.getLabels(docAttr.value, servletContext);
				for (int j=0; decodedLabels!=null && j<decodedLabels.length; j++){
					document.add(new Field(docAttr.name, decodedLabels[j], Field.Store.YES, Field.Index.TOKENIZED));
				}
			}
		}
		
		// create the "content" field, add it to the document
		if (contentBuf.toString().trim().length()>0)
			document.add(new Field("content", contentBuf.toString(), Field.Store.NO, Field.Index.TOKENIZED));
		
		// create the "IS_ENCODING_SCHEME" field, add it to the document
		document.add(new Field("IS_ENCODING_SCHEME", (hasRdfType && hasRdfsLabel) ? Boolean.TRUE.toString() : Boolean.FALSE.toString(),
				Field.Store.YES, Field.Index.TOKENIZED));
		
		// index the document into "index_all"
		allIndexWriter.updateDocument(new Term("ID", currentDocumentID), document);
		countDocumentsIndexed++;
		
		// if this document was an encoding scheme, add it into EncodingSchemes
		if (hasRdfType && hasRdfsLabel)
			EncodingSchemes.update(currentDocumentID, document.getValues("http://www.w3.org/2000/01/rdf-schema#label"), servletContext);
		
		// log succesful indexing
		logger.debug("Document indexed : " + currentDocumentID);
	}

	/**
	 * 
	 *
	 */
	private void reset(){
		currentDocumentID = null;
		currentDocumentAttributes = new ArrayList();
	}
	
	/**
	 * 
	 *
	 */
	private void handleError(Exception e){
		e.printStackTrace();
	}

	/**
	 * 
	 * @return
	 */
	public int getCountDocumentsIndexed() {
		return countDocumentsIndexed;
	}
	
	/**
	 * For the given resource return an ID that is ready to be stored into DB/index.
	 * If the resource is not anonymous, the method simply returns  <code>resource.getURI()</code>.
	 * If the resource is anonymous, an ID is generated that is unique across sources and time.
	 * 
	 * @param resource
	 * @return
	 */
	private String getResourceID(AResource resource) {
        
        if (resource.isAnonymous()){
            String anonID = resource.getAnonymousID();
            if (!currentAnonymousID.equals(anonID)){
                currentAnonymousID = anonID;
                currentGeneratedID = generateID(currentAnonymousID);
            }
            
            return currentGeneratedID;
        }
        else
            return resource.getURI();
    }

	/**
	 * 
	 * @param anonID
	 * @return
	 */
    private String generateID(String anonID) {
    	try{
    		StringBuffer bufHead = new StringBuffer("http://cr.eionet.europa.eu/anonymous/");
    		StringBuffer bufTail = new StringBuffer(String.valueOf(System.currentTimeMillis()));
    		bufTail.append(sourceURL==null ? "" : sourceURL.toString()).append(anonID);
    		return bufHead.append(Util.md5digest(bufTail.toString())).toString();
    	}
    	catch (GeneralSecurityException e){
    		throw new RuntimeException(e.toString(), e);
    	}
    }
    
	/**
	 * @return Returns the servletContext.
	 */
	public ServletContext getServletContext() {
		return servletContext;
	}

	/**
	 * @param servletContext The servletContext to set.
	 */
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
}