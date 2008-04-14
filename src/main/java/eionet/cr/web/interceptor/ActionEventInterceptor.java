package eionet.cr.web.interceptor;

import static eionet.cr.web.util.ICRWebConstants.LAST_ACTION_URL_SESSION_ATTR;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.ExecutionContext;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.Intercepts;
import net.sourceforge.stripes.controller.LifecycleStage;
import eionet.cr.util.StringUtils;
import eionet.cr.web.interceptor.annotation.DontSaveLastActionEvent;

/**
 * Interceptor that saves to the session last action except login action.
 * <p>
 * @author gerasvad
 *
 */
@Intercepts(value=LifecycleStage.EventHandling)
public class ActionEventInterceptor implements Interceptor {

	/* (non-Javadoc)
     * @see net.sourceforge.stripes.controller.Interceptor#intercept(net.sourceforge.stripes.controller.ExecutionContext)
     */
	@Override
	@SuppressWarnings("unchecked")
	public Resolution intercept(ExecutionContext context) throws Exception {
		Resolution resolution = null;
		
		Class<?> actionBeanClass = context.getActionBean().getClass();
		
		Method eventMethod = context.getHandler();
		
		if (context.getActionBeanContext().getValidationErrors().isEmpty() 
				&& !eventMethod.isAnnotationPresent(DontSaveLastActionEvent.class)) {
			
			HttpServletRequest request = context.getActionBean().getContext().getRequest(); 
			String actionEventURL = null;
			   
				actionEventURL = getActionName(actionBeanClass) + "?" + 
				((getEventName(eventMethod) != null) ? getEventName(eventMethod) + "=&" : "") + 
				getRequestParameters(request);
				
				//this will handle pretty url integration
				actionEventURL = postProcess(actionEventURL);
				
			request.getSession(true).setAttribute(LAST_ACTION_URL_SESSION_ATTR, actionEventURL);
		}

		resolution = context.proceed();
		return resolution;
	}
	
	private static String postProcess(String actionEventURL) {
		StringBuilder sb = new StringBuilder(actionEventURL);
		//first, let's split the string into 2 parts
		int actionEndPosition = actionEventURL.indexOf("?");
		String action = actionEventURL.substring(0, actionEndPosition);
		String params = actionEventURL.substring(actionEndPosition + 1);
		Matcher matcher = Pattern.compile("\\{([^\\{]*)\\}").matcher(action);
		while (matcher.find()) {
			//ok, let's start parsing   
			Matcher paramMatcher = Pattern.compile(matcher.group(1)+"=([^&]*)").matcher(params);
			//let's find a value to this one
			if(!paramMatcher.find()) {
				//didn't find a value for this param, cannot substitute anything
				continue;
			}
			String paramValue = paramMatcher.group(1); 
			int insertPoint = sb.indexOf(matcher.group(0)); 
			//replace the {xxx} stuff with paramValue
			sb.delete(insertPoint, insertPoint + matcher.group(0).length()).insert(insertPoint,paramValue);
			insertPoint = sb.indexOf(paramMatcher.group(0));
			//delete the parameter
			sb.delete(insertPoint,insertPoint +  paramMatcher.group(0).length());
		}
		//if we have a "?" at the end of the string, delete it
		if (sb.charAt(sb.length()-1) == '?') {
			sb.deleteCharAt(sb.length()-1);
		}
		
		return  sb.toString();
	}

	private static String getActionName(Class<?> actionBeanClass) {
		String result =  "/" + actionBeanClass.getName() + ".action";
		
		if (actionBeanClass.isAnnotationPresent(UrlBinding.class)) {
			UrlBinding urlBinding = actionBeanClass.getAnnotation(UrlBinding.class);
			result = urlBinding.value();
		}
		
		return result;
	}
	
	private static String getEventName(Method eventMethod) {
		if (eventMethod.isAnnotationPresent(DefaultHandler.class)) {
			return null;
		}
		String result = eventMethod.getName();
		
		if (eventMethod.isAnnotationPresent(HandlesEvent.class)) {
			HandlesEvent handlesEvent = eventMethod.getAnnotation(HandlesEvent.class);
			result = handlesEvent.value();
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static String getRequestParameters(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		Enumeration<String> parameters = request.getParameterNames();
		String parameter = null;
		String[] parameterValues = null;

		while(parameters.hasMoreElements()) {
			parameter = parameters.nextElement();
			parameterValues = request.getParameterValues(parameter);
			
			if(parameterValues == null || parameterValues.length == 0) {
				sb.append(parameter + "=&");
			} else {
				for (String parameterValue : parameterValues) {
					sb.append(parameter + "=" + 
							(StringUtils.isEmptyOrNull(parameterValue) ? "" : parameterValue) + 
							"&");
				}
			}
		}
		
		return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : sb.toString();
	}

}
