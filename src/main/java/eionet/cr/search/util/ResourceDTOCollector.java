package eionet.cr.search.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;

import eionet.cr.dto.ResourceDTO;

public class ResourceDTOCollector extends HitsCollector{

	/** */
	private List<ResourceDTO> resultList;

	/**
	 * @return the resultList
	 */
	public List<ResourceDTO> getResultList() {
		return resultList;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.util.HitsCollector#collectDocument(org.apache.lucene.document.Document)
	 */
	public void collectDocument(Document document){
		
		if (document==null)
			return;
		
		if (resultList==null)
			resultList = new ArrayList<ResourceDTO>();
		
		resultList.add(new ResourceDTO(document));
	}
}
