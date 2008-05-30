package eionet.cr.web.util;

import org.displaytag.decorator.TableDecorator;

import eionet.cr.dto.HarvestSourceDTO;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class HarvestSourcesTableDecorator extends TableDecorator{

	/**
	 * 
	 * @return
	 */
	public String getUrl(){
		
		HarvestSourceDTO harvestSource = (HarvestSourceDTO) getCurrentRowObject();
		StringBuffer buf = new StringBuffer();
		if (harvestSource.isUnavailable())
			buf.append("<img src=\"images/error.png\" alt=\"Errors\" title\"Source unavailable\"/>");
		buf.append(harvestSource.getUrl());
		return buf.toString();
	}
}
