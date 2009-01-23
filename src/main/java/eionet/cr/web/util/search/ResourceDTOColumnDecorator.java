package eionet.cr.web.util.search;


import java.util.List;

import javax.servlet.jsp.PageContext;

import org.displaytag.decorator.DisplaytagColumnDecorator;
import org.displaytag.exception.DecoratorException;
import org.displaytag.properties.MediaTypeEnum;

import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Util;
import eionet.cr.web.util.JstlFunctions;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class ResourceDTOColumnDecorator implements DisplaytagColumnDecorator{

	/*
	 * (non-Javadoc)
	 * @see org.displaytag.decorator.DisplaytagColumnDecorator#decorate(java.lang.Object, javax.servlet.jsp.PageContext, org.displaytag.properties.MediaTypeEnum)
	 */
	public Object decorate(Object columnValue, PageContext pageContext, MediaTypeEnum media) throws DecoratorException {
		
		if (columnValue==null || !(columnValue instanceof java.util.List))
			return "";
		else{
			List<String> list = SubjectDTO.asDistinctLiteralValues((java.util.List)columnValue);
			String s = Util.toCsvString(list);
			return JstlFunctions.cutAtFirstLongToken(s, 50);
		}
	}
}
