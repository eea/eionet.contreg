package eionet.cr.util;

import java.util.HashMap;
import java.util.List;

import eionet.cr.common.Namespace;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.PredicateDTO;

public class NamespaceUtil {

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
	
	public static String extractPredicate(String url){
		if (url==null)
			return null;
		
		int i = url.lastIndexOf("#");
		if (i<0){
			i = url.lastIndexOf("/");
		}
		
		return i<0 ? null : url.substring(i + 1, url.length());
	}
	
	/**
	 * 
	 * @param url
	 * @param prefix
	 */
	public static void addNamespace(HashMap<Long, String> namespaces, Namespace namespace){
		namespaces.put(Hashes.spoHash(namespace.getUri()), namespace.getPrefix());
	}
	
	public static String getKnownNamespace(String namespace){
		
		Namespace knownNamespaces[] = Namespace.values();
		
		for (Namespace singleNamespace:knownNamespaces){
			if (singleNamespace.getUri().equals(namespace)){
				return singleNamespace.getPrefix();
			}
		}
		
		return null;
	}
	
	
	public static HashMap <Long, String> getNamespacePrefix(List<PredicateDTO> distinctPredicates) throws DAOException{
		
		HashMap <Long, String> returnValues = new HashMap<Long, String>();
		
		int unknownNamespaceCounter = 0;
		
		for(PredicateDTO predicate:distinctPredicates){
			String namespace = NamespaceUtil.extractNamespace(predicate.getValue());
			String knownNamespacePrefix = NamespaceUtil.getKnownNamespace(namespace);
			
			if (knownNamespacePrefix == null || knownNamespacePrefix.isEmpty()){
				unknownNamespaceCounter ++;
				returnValues.put(Hashes.spoHash(namespace), "ns"+unknownNamespaceCounter);
				System.out.println("New namespace: "+"ns"+unknownNamespaceCounter);
			} else {
				returnValues.put(Hashes.spoHash(namespace), knownNamespacePrefix);
				System.out.println("Known namespace: "+knownNamespacePrefix);
			}
		}
		return returnValues;
	}

	
}
