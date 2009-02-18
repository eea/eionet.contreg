package eionet.cr.web.util;

import java.text.SimpleDateFormat;

import org.displaytag.decorator.TableDecorator;

import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.scheduled.HarvestingJob;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class HarvestSourcesTableDecorator extends TableDecorator{
	
	/** */
	private SimpleDateFormat lastHarvestDatetimeFormat = new SimpleDateFormat("dd-MM-yy HH:mm:ss");

	/**
	 * 
	 * @return
	 */
	public String getUrl(){
		
		StringBuffer buf = new StringBuffer();
		String url = ((HarvestSourceDTO) getCurrentRowObject()).getUrl();
		if (url!=null){
			buf.append("<a class=\"link-plain\" href=\"source.action?view=&harvestSource.url=").
			append(url).append("\">").append(url).append("</a>");
		}
		
		return buf.toString();
	}

	/**
	 * 
	 * @return
	 */
	public String getLastHarvestDatetime(){
		
		HarvestSourceDTO harvestSource = (HarvestSourceDTO) getCurrentRowObject();
		if (harvestSource.getLastHarvestDatetime()!=null)
			return lastHarvestDatetimeFormat.format(harvestSource.getLastHarvestDatetime());
		else
			return "&nbsp;";
	}
}
