package eionet.cr.web.action;

import org.junit.Test;

import eionet.cr.test.util.AbstractStripesMvcTestHelper; 
import net.sourceforge.stripes.mock.MockServletContext;
import net.sourceforge.stripes.mock.MockRoundtrip;
import static org.junit.Assert.*;


public class TestCalculatorActionBean extends AbstractStripesMvcTestHelper {
	@Test
	public void positiveTest() throws Exception {
	    // Setup the servlet engine
	    MockServletContext ctx = getMockServletContext();

	    MockRoundtrip trip = new MockRoundtrip(ctx, CalculatorActionBean.class);
	    trip.setParameter("numberOne", "2");
	    trip.setParameter("numberTwo", "2");
	    trip.execute("addition");

	    CalculatorActionBean bean = trip.getActionBean(CalculatorActionBean.class);
	    assertEquals(bean.getResult(), 4.0);
	    assertEquals(trip.getDestination(), "/pages/calculator.jsp");
	}
}
