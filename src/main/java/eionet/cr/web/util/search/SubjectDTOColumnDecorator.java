package eionet.cr.web.util.search;


import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

import javax.servlet.jsp.PageContext;

import org.displaytag.decorator.DisplaytagColumnDecorator;
import org.displaytag.exception.DecoratorException;
import org.displaytag.properties.MediaTypeEnum;

import eionet.cr.dto.ObjectDTO;
import eionet.cr.web.util.JstlFunctions;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SubjectDTOColumnDecorator implements DisplaytagColumnDecorator{

	/*
	 * (non-Javadoc)
	 * @see org.displaytag.decorator.DisplaytagColumnDecorator#decorate(java.lang.Object, javax.servlet.jsp.PageContext, org.displaytag.properties.MediaTypeEnum)
	 */
	public Object decorate(Object columnValue, PageContext pageContext, MediaTypeEnum media) throws DecoratorException {
		
		if (columnValue==null)
			return "";
		else if (!(columnValue instanceof Collection))
			return columnValue.toString();
		else
			return JstlFunctions.cutAtFirstLongToken(format((Collection)columnValue), 50);
	}

	/**
	 * 
	 * @param coll
	 * @return
	 */
	public static String format(Collection coll){
		
		LinkedHashSet hashSet = new LinkedHashSet(coll);
		StringBuffer bufLiterals = new StringBuffer();
		StringBuffer bufNonLiterals = new StringBuffer();
		
		for (Iterator iter = hashSet.iterator(); iter.hasNext();){			
			Object o = iter.next();
			if (o instanceof ObjectDTO){
				ObjectDTO objectDTO = (ObjectDTO)o;
				if (objectDTO.isLiteral())
					append(bufLiterals, objectDTO.toString());
				else
					append(bufNonLiterals, objectDTO.toString());
			}
			else
				append(bufLiterals, o.toString());
		}
		
		return bufLiterals.length()>0 ? bufLiterals.toString() : bufNonLiterals.toString();
	}
	
	/**
	 * 
	 * @param buf
	 * @param s
	 */
	private static void append(StringBuffer buf, String s){
		if (s.trim().length()>0){
			if (buf.length()>0){
				buf.append(", ");
			}
			buf.append(s);
		}
	}
}
