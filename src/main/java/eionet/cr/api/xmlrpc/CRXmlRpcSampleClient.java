/*
 * The contents of this file are subject to the Mozilla Public
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
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.api.xmlrpc;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import eionet.cr.util.Util;
import eionet.qawcommons.DataflowResultDto;

/**
 * 
 * @author heinljab
 *
 */
public class CRXmlRpcSampleClient {
	
	/**
	 * @throws XmlRpcException 
	 * @throws MalformedURLException 
	 * 
	 */
	public static void sample_getResourcesSinceTimestamp() throws XmlRpcException, MalformedURLException{
		
		// set up the XmlRpcClient
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
	    config.setServerURL(new URL("http://ww010646:8080/cr/xmlrpc"));
	    config.setEnabledForExtensions(true);
	    XmlRpcClient client = new XmlRpcClient();
	    client.setConfig(config);
	    
	    // execute the call
	    Object[] params = new Object[]{Util.stringToDate("2010-01-01 00:00:00", "yyyy-MM-dd HH:mm:ss")};
	    Object[] result = (Object[])client.execute("ContRegService.getResourcesSinceTimestamp", params);
	    
	    // loop through the results, do type casting to see if any ClassCastExceptions are thrown
	    if (result!=null && result.length>0){
	    	
		    for (int i=0; i<result.length; i++){
		    	Map<String,Object[]> map = (Map<String,Object[]>)result[i];
		    	if (map!=null && !map.isEmpty()){
			    	Iterator<String> keys = map.keySet().iterator();
			    	while (keys.hasNext()){
			    		String key = keys.next();
			    		Object[] values = map.get(key);
			    		StringBuffer buf = new StringBuffer(values==null ? "null array" : "");
			    		for (int j=0; values!=null && j<values.length; j++){
			    			if (j>0)
			    				buf.append(", ");
			    			buf.append(values[j].toString());
			    		}
			    		System.out.println(key + " = " + buf.toString());
			    	}
		    	}
		    	else
		    		System.out.println("map null or empty");
		    	System.out.println("====================================================================================");
		    }
	    }
	    else
	    	System.out.println("result array null or empty");

	}
	
	/**
	 * 
	 * @throws MalformedURLException
	 * @throws XmlRpcException
	 */
	public static void sample_dataflowSearch() throws MalformedURLException, XmlRpcException{
		
		// set up the XmlRpcClient
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
	    //config.setServerURL(new URL("http://80.235.29.171:8080/cr/xmlrpc"));
		config.setServerURL(new URL("http://localhost:8080/cr/xmlrpc"));
	    config.setEnabledForExtensions(true);
	    XmlRpcClient client = new XmlRpcClient();
	    client.setConfig(config);
	    
	    // execute the call
	    Map criteria = new HashMap();
	    criteria.put("http://rod.eionet.europa.eu/schema.rdf#locality", "http://rod.eionet.europa.eu/spatial/28"); // Norway
	    criteria.put("http://rod.eionet.europa.eu/schema.rdf#obligation", "http://rod.eionet.europa.eu/obligations/452");
	    criteria.put("http://purl.org/dc/elements/1.1/coverage", "2006");
	    
	    Object[] params = new Object[]{criteria};
	    Object[] result = (Object[])client.execute("ContRegService.dataflowSearch", params);
	    
	    // get the first object in the result set
	    if (result!=null && result.length>0){
	    	
	    	Object o = result[0];
	    	DataflowResultDto dto = (DataflowResultDto)o;
	    	System.out.println(o);
	    }
	    else
	    	System.out.println("result array null or empty");
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		
		try{
			sample_getResourcesSinceTimestamp();
			//sample_simpleAndSearch();
			//sample_dataflowSearch();
		}
		catch (Throwable t){
			t.printStackTrace();
		}
	}
}
