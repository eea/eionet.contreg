package eionet.cr.web.util;

import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringEscapeUtils;
import org.displaytag.decorator.TableDecorator;

import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.util.Util;

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
			buf.append("<a class=\"link-plain\" href=\"source.action?view=&amp;harvestSource.url=").
			append(Util.urlEncode(url)).append("\">").append(StringEscapeUtils.escapeXml(url)).append("</a>");
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
