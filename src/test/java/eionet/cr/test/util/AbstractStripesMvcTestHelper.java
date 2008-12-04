package eionet.cr.test.util;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.stripes.controller.DispatcherServlet;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.mock.MockServletContext;
import eionet.cr.web.action.AbstractCRActionBean;

/**
 * Helper class for testing Stripes action beans.
 * 
 * @author gerasvad, altnyris
 *
 */
public abstract class AbstractStripesMvcTestHelper extends AbstractCRActionBean {
	private static MockServletContext mockServletContext;
	
	protected MockServletContext getMockServletContext() {
		if (mockServletContext == null) {
			initMockServletContext();
		}
		
		return mockServletContext;
	}
	
	private void initMockServletContext() {
		mockServletContext = new MockServletContext("test");

		// Add the Stripes Filter
		Map<String,String> filterParams = new HashMap<String,String>();
		filterParams.put("ActionResolver.UrlFilters", "target/classes, target/test-classes");
		filterParams.put("ActionResolver.PackageFilters", "eionet.cr.web.action.*");
		filterParams.put("ActionBeanContext.Class", "eionet.cr.web.context.CRActionBeanContext");
		mockServletContext.addFilter(StripesFilter.class, "StripesFilter", filterParams);

		// Add the Stripes Dispatcher
		mockServletContext.setServlet(DispatcherServlet.class, "StripesDispatcher", null);
	}
}
