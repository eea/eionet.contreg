/*
* The contents of this file are subject to the Mozilla Public
* 
* License Version 1.1 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of
* the License at http://www.mozilla.org/MPL/
* 
* Software distributed under the License is distributed on an "AS
* IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
* implied. See the License for the specific language governing
* rights and limitations under the License.
* 
* The Original Code is Content Registry 2.0.
* 
* The Initial Owner of the Original Code is European Environment
* Agency. Portions created by Tieto Eesti are Copyright
* (C) European Environment Agency. All Rights Reserved.
* 
* Contributor(s):
* Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.api.feeds;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.common.Namespaces;
import eionet.cr.common.SubjectProcessor;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.URLUtil;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SubjectsRDFWriter {
	
	/** */
	private HashMap<String,String> namespaces = new HashMap<String, String>();
	
	/** */
	private String xmlLang;
	
	/** */
	private SubjectProcessor subjectProcessor;
	
	/** */
	private boolean includeDerivedValues = false;

	/**
	 * 
	 */
	public SubjectsRDFWriter(){
		
		namespaces.put(Namespaces.RDF, "rdf");
		namespaces.put(Namespaces.RDFS, "rdfs");
	}

	/**
	 * 
	 */
	public SubjectsRDFWriter(boolean includeDerivedValues){
		
		this();		
		this.includeDerivedValues = includeDerivedValues;
	}
	
	/**
	 * 
	 * @param url
	 * @param prefix
	 */
	public void addNamespace(String url, String prefix){
		
		if (url==null || url.trim().length()==0 || prefix==null || prefix.trim().length()==0){
			throw new IllegalArgumentException("Url and prefix must not be blank!");
		}
		
		namespaces.put(url, prefix);
	}
	
	/**
	 * 
	 * @return
	 */
	private String getAttributes(){
		
		StringBuffer buf = new StringBuffer("");
		if (xmlLang!=null && xmlLang.trim().length()>0){
			buf.append(" xml:lang=\"").append(xmlLang).append("\"");
		}
		
		if (!namespaces.isEmpty()){
			for (Entry<String, String> entry : namespaces.entrySet()){
				buf.append("\n   xmlns:").append(entry.getValue()).append("=\"").append(entry.getKey()).append("\"");
			}
		}
		
		return buf.toString();
	}
	
	/**
	 * 
	 * @return
	 */
	private String getRootElementEnd(){
		return "</rdf:RDF>";
	}

	/**
	 * @param xmlLang the xmlLang to set
	 */
	public void setXmlLang(String xmlLang) {
		this.xmlLang = xmlLang;
	}
	
	/**
	 * 
	 * @param subjects
	 * @param out
	 * @throws IOException 
	 */
	public void write(List<SubjectDTO> subjects, OutputStream out) throws IOException{

		if (subjects==null || subjects.isEmpty()){
			out.write("<rdf:RDF/>".getBytes());
			return;
		}
		
		out.write(("<rdf:RDF" + getAttributes() + ">").getBytes());
		
		for (SubjectDTO subject:subjects){
			
			if (subjectProcessor!=null){
				subjectProcessor.process(subject);
			}
			
			if (subject.getPredicateCount()>0){
				
				StringBuffer buf = new StringBuffer("\n\t<rdf:Description rdf:about=\"");
				buf.append(subject.getUri()).append("\">");
				
				for (Entry<String,Collection<ObjectDTO>> entry : subject.getPredicates().entrySet()){

					String predicate = entry.getKey();
					
					if (entry.getValue()!=null && !entry.getValue().isEmpty()){
						
						String nsUrl = extractNamespace(predicate);
						if (nsUrl==null || nsUrl.trim().length()==0){
							throw new CRRuntimeException("Could not extract namespace URL from " + predicate);
						}
						
						// include only predicates from supplied namespaces
						if (namespaces.containsKey(nsUrl)){
							
							String localName = StringUtils.substringAfterLast(predicate, nsUrl);
							if (localName==null || localName.trim().length()==0){
								throw new CRRuntimeException("Could not extract local name from " + predicate);
							}

							HashSet<String> alreadyWritten = new HashSet<String>();
							for (ObjectDTO object : entry.getValue()){

								// include only non-blank and non-derived objects that have not been already written
								if (!StringUtils.isBlank(object.getValue())
										&& !alreadyWritten.contains(object.getValue())
										&& (includeDerivedValues || object.getDerivSourceHash()==0)){

									buf.append("\n\t\t<").append(namespaces.get(nsUrl)).append(":").append(localName);

									String escapedValue = StringEscapeUtils.escapeXml(object.getValue());
									if (!object.isLiteral() && URLUtil.isURL(object.getValue())){
										
										buf.append(" rdf:resource=\"").append(escapedValue).append("\"/>");
									}
									else{
										buf.append(">").
										append(escapedValue).
										append("</").append(namespaces.get(nsUrl)).append(":").append(localName).append(">");
									}
									
									alreadyWritten.add(object.getValue());
								}
							}
						}
					}
				}
				
				buf.append("\n\t</rdf:Description>");
				out.write(buf.toString().getBytes());
			}
		}
		
		out.write("</rdf:RDF>\n".getBytes());
	}
	
	/**
	 * 
	 * @param url
	 * @return
	 */
	public static String extractNamespace(String url){
		
		if (url==null)
			return null;
		
		int i = url.lastIndexOf("#");
		if (i<0){
			i = url.lastIndexOf("/");
		}
		
		return i<0 ? null : url.substring(0, i+1);
	}
	
	/**
	 * 
	 * @param subject
	 */
	protected void preProcessSubject(SubjectDTO subject){
	}

	/**
	 * @param subjectProcessor the subjectProcessor to set
	 */
	public void setSubjectProcessor(SubjectProcessor subjectProcessor) {
		this.subjectProcessor = subjectProcessor;
	}
}
