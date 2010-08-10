package eionet.cr.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;

public class SubjectDTOOptimizer {
	
	public static SubjectDTO optimizeSubjectDTOFactsheetView(SubjectDTO subject, List<String> languages){
		
		SubjectDTO returnSubject = subject;
		if (subject != null){
			Map<String,Collection<ObjectDTO>> predicates = subject.getPredicates();
			Iterator it = predicates.entrySet().iterator();
			
		    while (it.hasNext()) {
		        Map.Entry pairs = (Map.Entry)it.next();
		        Collection<ObjectDTO> filteredObjects = new ArrayList<ObjectDTO>();
		        Collection<ObjectDTO> foundResources = new ArrayList<ObjectDTO>();
		        Collection<ObjectDTO> objects = subject.getObjects(pairs.getKey().toString());
		    
		        // STEP 1
		        // First, let's find out all the resources we have in the list and add them to the new query.
		        // Resource is identified by not being Literal.
		        for (ObjectDTO object:objects){
		        	if (!object.isLiteral()){
		        		filteredObjects.add(object);
		        		foundResources.add(object);
		        	}
		        }
	
		        // STEP 2
		        // We must extend the language list in order to include also these languages that were left out before
		        for (ObjectDTO object:objects){
		        	boolean languageAlreadyListed = false;
		        	for (int a=0; a<languages.size(); a++){
		        		if (object.getLanguage().equals(languages.get(a)) ){
		        			languageAlreadyListed = true;
		        			break;
		        		}
		        	}
		        	if (!languageAlreadyListed){
		        		languages.add(object.getLanguage());
		        	}
		        }
		        
		        // STEP 3
		        // Now, let's try to find a suitable label for each resource already in filteredObjects list.
		        // Suitable label is literal with obj_source_obj == object.source 
		        for (ObjectDTO object:foundResources){
		        	boolean suitableLabelFound = false;
			        for (int a=0; a<languages.size(); a++){
			        	for (ObjectDTO labelObject:objects){
			        		if (labelObject.getSourceObjectHash() == object.getHash() && labelObject.isLiteral() && labelObject.getLanguage().equals(languages.get(a))&& suitableLabelFound == false){
			        			filteredObjects.add(labelObject);
			        			suitableLabelFound = true;
			        			break;
			        		}
			        	}
			        	if (suitableLabelFound){
			        		break;
			        	}
			        }
		        }
	        
		        // STEP 4
		        // Now let's find all the literals that aren't labels for any resource.
		        // We are going to add them based on the language priority.
		        for (int a=0; a<languages.size(); a++){
			        for (ObjectDTO object:objects){
			        	if (object.isLiteral() && object.getSourceObjectHash() == 0 && object.getLanguage().equals(languages.get(a))){
			        		
			        		// Now testing whether an object with exactly the same literal value is already present (with higher ranked language).
			        		boolean currentValuePresent = false;
			        		for (ObjectDTO literalObject:filteredObjects){
			        			if (literalObject.getHash() == object.getHash()){
			        				currentValuePresent = true;
			        			}
			        		}
			        		if (!currentValuePresent){
			        			filteredObjects.add(object);
			        		}
			        	}
			        }
		        }
		        predicates.put(pairs.getKey().toString(), filteredObjects);
		    }
		}
		
		return returnSubject;
	}

}
