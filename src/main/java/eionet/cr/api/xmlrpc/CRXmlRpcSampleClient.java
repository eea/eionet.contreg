package eionet.cr.api.xmlrpc;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import eionet.cr.util.Util;

/**
 * 
 * @author heinljab
 *
 */
public class CRXmlRpcSampleClient {

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		
		try{
			// set up the XmlRpcClient
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		    config.setServerURL(new URL("http://127.0.0.1:8080/cr/xmlrpc"));
		    config.setEnabledForExtensions(true);
		    XmlRpcClient client = new XmlRpcClient();
		    client.setConfig(config);
		    
		    // execute the call
		    Object[] params = new Object[]{Util.stringToDate("2007-01-01 10:30:00", "yyyy-MM-dd HH:mm:ss")};
		    Object[] result = (Object[])client.execute("Searcher.getResourcesSinceTimestamp", params);
		    
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
		catch (Throwable t){
			t.printStackTrace();
		}
	}
}
