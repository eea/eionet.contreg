package eionet.cr.harvest;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Hits;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdf.arp.ALiteral;
import com.hp.hpl.jena.rdf.arp.AResource;
import com.hp.hpl.jena.rdf.arp.StatementHandler;

import eionet.cr.harvest.util.RDFResource;
import eionet.cr.harvest.util.RDFResourceProperty;
import eionet.cr.harvest.util.WrappedARPObject;
import eionet.cr.index.EncodingSchemes;
import eionet.cr.index.Searcher;
import eionet.cr.util.Identifiers;
import eionet.cr.util.Util;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class RDFHandler implements StatementHandler, org.xml.sax.ErrorHandler{

	/** */
	private static Log logger = LogFactory.getLog(RDFHandler.class);
	
	/** */
	private String currentDocumentID;
	private ArrayList currentDocumentAttributes;
	private int countDocumentsIndexed = 0;
	
	/** */
	private String currentAnonymousID = "";
	private String currentGeneratedID = "";
	private String sourceUrlString = null;
	
	/** */
	private SAXParseException fatalError = null;
	
	/** */
	private Map<String,RDFResource> rdfResources = new HashMap<String,RDFResource>();

	/** */
	private List<SAXParseException> errors = new ArrayList<SAXParseException>();
	private List<SAXParseException> warnings = new ArrayList<SAXParseException>();

	/**
	 * 
	 */
	public RDFHandler(String sourceUrlString){
		
		if (sourceUrlString==null)
			throw new NullPointerException();
		
		this.sourceUrlString = sourceUrlString;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see com.hp.hpl.jena.rdf.arp.StatementHandler#statement(com.hp.hpl.jena.rdf.arp.AResource, com.hp.hpl.jena.rdf.arp.AResource, com.hp.hpl.jena.rdf.arp.AResource)
	 */
	public void statement(AResource subject, AResource predicate, AResource object) {
		
		if (isFatalError() || subject==null || predicate==null || object==null)
			return;

		statement(subject, predicate, new WrappedARPObject(object, getResourceID(object)));
	}

	/*
	 *  (non-Javadoc)
	 * @see com.hp.hpl.jena.rdf.arp.StatementHandler#statement(com.hp.hpl.jena.rdf.arp.AResource, com.hp.hpl.jena.rdf.arp.AResource, com.hp.hpl.jena.rdf.arp.ALiteral)
	 */
	public void statement(AResource subject, AResource predicate, ALiteral object) {

		if (isFatalError() || subject==null || predicate==null || object==null)
			return;

		statement(subject, predicate, new WrappedARPObject(object));
	}
	
	/**
	 * 
	 * @param subject
	 * @param predicate
	 * @param object
	 * @throws HarvestException 
	 */
	protected void statement(AResource subject, AResource predicate, WrappedARPObject object){
		
		String subjectId = getResourceID(subject);
		String predicateId = getResourceID(predicate);
		if (subjectId==null || predicateId==null)
			return;

		RDFResource resource = rdfResources.get(subjectId);
		if (resource==null)
			resource = new RDFResource(subjectId, sourceUrlString);
		resource.addProperty(new RDFResourceProperty(predicateId, object.getStringValue(), object.isLiteral(), object.isAnonymous()));
		rdfResources.put(subjectId, resource);
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
		StringBuffer bufHead = new StringBuffer(Identifiers.ANON_ID_PREFIX);
		StringBuffer bufTail = new StringBuffer(String.valueOf(System.currentTimeMillis()));
		bufTail.append(sourceUrlString==null ? "" : sourceUrlString).append(anonID);
		return bufHead.append(Util.md5digest(bufTail.toString())).toString();
    }

    /*
     * (non-Javadoc)
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
	public void error(SAXParseException e) throws SAXException {
		errors.add(e);
		logger.error("SAX error encountered: " + e.toString(), e);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
	 */
	public void fatalError(SAXParseException e) throws SAXException {
		fatalError = e;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
	 */
	public void warning(SAXParseException e) throws SAXException {
		warnings.add(e);
		logger.warn("SAX warning encountered: " + e.toString(), e);
	}

	/**
	 * @return the fatalError
	 */
	public SAXParseException getFatalError() {
		return fatalError;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isFatalError(){
		return fatalError!=null;
	}

	/**
	 * @return the rdfResources
	 */
	public Map<String, RDFResource> getRdfResources() {
		return rdfResources;
	}

	/**
	 * @return the errors
	 */
	public List<SAXParseException> getErrors() {
		return errors;
	}

	/**
	 * @return the warnings
	 */
	public List<SAXParseException> getWarnings() {
		return warnings;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getCountResources(){
		return rdfResources.size();
	}
}