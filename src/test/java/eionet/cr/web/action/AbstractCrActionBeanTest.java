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
 * Risto Alt, Tieti Eesti
 */
package eionet.cr.web.action;

import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;

import org.junit.Assert;
import org.junit.Test;

import eionet.cr.test.helpers.AbstractStripesMvcTestHelper;
import eionet.cr.web.security.CRUser;
import eionet.cr.web.util.WebConstants;

/**
 * JUnit test tests AbstractQawActionBean functionality.
 *
 * @author gerasvad, altnyris
 *
 */
public class AbstractCrActionBeanTest extends AbstractStripesMvcTestHelper {

    /**
     * Tests method {@link AbstractCrActionBean#getUserName()} when user is logged in.
     *
     * @throws Exception
     */
    @Test
    public void testGetUserNameWhenLoggedIn() throws Exception {
        MockServletContext context = getMockServletContext();

        MockRoundtrip trip = new MockRoundtrip(context, LoginActionBean.class);
        trip.getRequest().getSession().setAttribute(WebConstants.USER_SESSION_ATTR, new CRUser("smithbob"));
        trip.execute();

        LoginActionBean actionBean = trip.getActionBean(LoginActionBean.class);

        Assert.assertEquals("smithbob", actionBean.getUserName());
    }

    /**
     * Tests method {@link AbstractCrActionBean#isUserLoggedIn()} when user is logged in.
     *
     * @throws Exception
     */
    @Test
    public void testIsUserLoggedInTrue() throws Exception {
        MockServletContext context = getMockServletContext();

        MockRoundtrip trip = new MockRoundtrip(context, LoginActionBean.class);
        trip.getRequest().getSession().setAttribute(WebConstants.USER_SESSION_ATTR, new CRUser("smithbob"));
        trip.execute();

        LoginActionBean actionBean = trip.getActionBean(LoginActionBean.class);

        Assert.assertEquals(true, actionBean.isUserLoggedIn());
    }

    /**
     * Tests method {@link AbstractCrActionBean#isUserLoggedIn()} when user is not logged in.
     *
     * @throws Exception
     */
    @Test
    public void testIsUserLoggedInFalse() throws Exception {
        MockServletContext context = getMockServletContext();

        MockRoundtrip trip = new MockRoundtrip(context, LoginActionBean.class);
        trip.getRequest().getSession().setAttribute(WebConstants.USER_SESSION_ATTR, null);
        trip.execute();

        LoginActionBean actionBean = trip.getActionBean(LoginActionBean.class);

        Assert.assertEquals(false, actionBean.isUserLoggedIn());
    }
}
