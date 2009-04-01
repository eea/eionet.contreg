package eionet.cr.web.util;

import java.text.SimpleDateFormat;

import org.displaytag.decorator.TableDecorator;

import eionet.cr.dto.HarvestSourceDTO;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class HarvestSourcesTableDecorator extends TableDecorator{
	
	/** */
	private SimpleDateFormat lastHarvestFormat = new SimpleDateFormat("dd-MM-yy HH:mm:ss");

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
	public String getLastHarvest(){
		
		HarvestSourceDTO harvestSource = (HarvestSourceDTO) getCurrentRowObject();
		if (harvestSource.getLastHarvest()!=null)
			return lastHarvestFormat.format(harvestSource.getLastHarvest());
		else
			return "&nbsp;";
	}
}
