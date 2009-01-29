package eionet.cr.web.action;

import static eionet.cr.web.util.WebConstants.USER_SESSION_ATTR;
import static org.junit.Assert.assertEquals;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;

import org.junit.Test;

import eionet.cr.test.util.AbstractStripesMvcTestHelper;
import eionet.cr.web.security.CRUser;


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
		trip.getRequest().getSession().setAttribute(USER_SESSION_ATTR, new CRUser("smithbob"));
		trip.execute();
		
		LoginActionBean actionBean = trip.getActionBean(LoginActionBean.class);
		
		assertEquals("smithbob", actionBean.getUserName());
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
		trip.getRequest().getSession().setAttribute(USER_SESSION_ATTR, new CRUser("smithbob"));
		trip.execute();
		
		LoginActionBean actionBean = trip.getActionBean(LoginActionBean.class);
		
		assertEquals(true, actionBean.isUserLoggedIn());
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
		trip.getRequest().getSession().setAttribute(USER_SESSION_ATTR, null);
		trip.execute();
		
		LoginActionBean actionBean = trip.getActionBean(LoginActionBean.class);
		
		assertEquals(false, actionBean.isUserLoggedIn());
	}
}
