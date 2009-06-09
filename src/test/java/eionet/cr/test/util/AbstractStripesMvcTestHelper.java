/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Vadim Gerassimov, Tieto Eesti
 * Risto Alt, Tieto Eesti
 */
package eionet.cr.test.util;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.stripes.controller.DispatcherServlet;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.mock.MockServletContext;
import eionet.cr.web.action.AbstractActionBean;

/**
 * Helper class for testing Stripes action beans.
 * 
 * @author gerasvad, altnyris
 *
 */
public abstract class AbstractStripesMvcTestHelper extends AbstractActionBean {
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
