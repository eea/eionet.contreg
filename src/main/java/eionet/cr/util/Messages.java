package eionet.cr.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class Messages extends ArrayList<Object>{

	/**
	 *
	 */
	public Messages(){
		super();
	}

	/**
	 * 
	 * @param msg
	 */
	public Messages(String msg){
		super();
		add(msg);
	}
	
	/**
	 * 
	 * @param msgs
	 */
	public Messages(String[] msgs){
		super(Arrays.asList(msgs));
	}

	/**
	 * 
	 * @param servletRequest
	 * @param attrName
	 * @param message
	 */
	public static void addMessage(HttpServletRequest servletRequest, String attrName, String message){
		if (servletRequest!=null && message!=null && attrName!=null){
			Messages messages = (Messages)servletRequest.getAttribute(attrName);
			if (messages==null)
				messages = new Messages();
			messages.add(message);
			servletRequest.setAttribute(attrName, messages);
		}
	}
}
