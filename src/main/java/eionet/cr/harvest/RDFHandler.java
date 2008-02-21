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

import eionet.cr.harvest.util.RDFResource;
import eionet.cr.harvest.util.RDFResourceProperty;
import eionet.cr.harvest.util.WrappedARPObject;
import eionet.cr.index.EncodingSchemes;
import eionet.cr.index.Searcher;
import eionet.cr.util.Util;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class RDFHandler implements StatementHandler{

	/** */
	private static Logger logger = Logger.getLogger(RDFHandler.class);
	
	private String currentDocumentID;
	private ArrayList currentDocumentAttributes;
	private int countDocumentsIndexed = 0;
	
	/** */
	private String currentAnonymousID = "";
	private String currentGeneratedID = "";
	private URL sourceURL = null;
	
	/** */
	private RDFResource currentResource = null;
	private HarvestListener harvestListener = null;
	
	/**
	 * 
	 */
	public RDFHandler(URL sourceURL, HarvestListener harvestListener){
		this.sourceURL = sourceURL;
		this.harvestListener = harvestListener;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see com.hp.hpl.jena.rdf.arp.StatementHandler#statement(com.hp.hpl.jena.rdf.arp.AResource, com.hp.hpl.jena.rdf.arp.AResource, com.hp.hpl.jena.rdf.arp.AResource)
	 */
	public void statement(AResource subject, AResource predicate, AResource object) {
		
		if (subject==null || predicate==null || object==null)
			return;
		
		statement(subject, predicate, new WrappedARPObject(object, getResourceID(object)));
	}

	/*
	 *  (non-Javadoc)
	 * @see com.hp.hpl.jena.rdf.arp.StatementHandler#statement(com.hp.hpl.jena.rdf.arp.AResource, com.hp.hpl.jena.rdf.arp.AResource, com.hp.hpl.jena.rdf.arp.ALiteral)
	 */
	public void statement(AResource subject, AResource predicate, ALiteral object) {

		if (subject==null || predicate==null || object==null)
			return;
		
		statement(subject, predicate, new WrappedARPObject(object));
	}
	
	/**
	 * 
	 * @param subject
	 * @param predicate
	 * @param object
	 */
	public void statement(AResource subject, AResource predicate, WrappedARPObject object){
		
		String subjectID = getResourceID(subject);
		String predicateID = getResourceID(predicate);
		if (subjectID==null || predicateID==null)
			return;
		
		if (currentResource!=null && !currentResource.getId().equals(subjectID)){
			if (harvestListener!=null)
				harvestListener.resourceHarvested(currentResource);
			currentResource = new RDFResource(subjectID);
		}
		
		RDFResourceProperty property = new RDFResourceProperty(predicateID, object.getStringValue(), object.isLiteral(), object.isAnonymous());
		currentResource.addProperty(property);
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
	 * 
	 * @return
	 */
	public int getCountDocumentsIndexed() {
		return countDocumentsIndexed;
	}
}