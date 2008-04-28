package eionet.cr.web.util.search;


import javax.servlet.jsp.PageContext;

import org.displaytag.exception.DecoratorException;
import org.displaytag.properties.MediaTypeEnum;

import eionet.cr.common.ResourceDTO;
import eionet.cr.util.Util;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class ResourceDTOColumnDecorator extends CsvDecorator{

	/**
	 * 
	 */
	public Object decorate(Object columnValue, PageContext pageContext, MediaTypeEnum media) throws DecoratorException {
		
		if (columnValue==null || !(columnValue instanceof java.util.List))
			return "";
		else
			return super.decorate(ResourceDTO.asDistinctLiteralValues((java.util.List)columnValue), pageContext, media);
	}
}
