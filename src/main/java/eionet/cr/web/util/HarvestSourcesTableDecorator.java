package eionet.cr.web.util;

import org.displaytag.decorator.TableDecorator;

import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.scheduled.HarvestingJob;

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
		String currentlyHarvestedUrl = HarvestingJob.getCurrentlyHarvestedItem()==null ? "" : HarvestingJob.getCurrentlyHarvestedItem().getUrl();
		if (currentlyHarvestedUrl.equals(harvestSource.getUrl()))
			buf.append("<img src=\"images/animated-loader.gif\" alt=\"Harvesting\" title\"Currently being harvested\"/>");
		buf.append(harvestSource.getUrl());
		return buf.toString();
	}
}
