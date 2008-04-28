package eionet.cr.search.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;

import eionet.cr.common.ResourceDTO;
import eionet.cr.util.DocumentListener;

public class ResourceDTOCollector implements DocumentListener{

	/** */
	private List<ResourceDTO> resultList;

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.DocumentListener#handleDocument(org.apache.lucene.document.Document)
	 */
	public void handleDocument(Document document){
		
		if (document==null)
			return;
		
		if (resultList==null)
			resultList = new ArrayList<ResourceDTO>();
		
		resultList.add(new ResourceDTO(document));
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.DocumentListener#done()
	 */
	public void done() {
	}

	/**
	 * @return the resultListAAA
	 */
	public List<ResourceDTO> getResultList() {
		return resultList;
	}
}
