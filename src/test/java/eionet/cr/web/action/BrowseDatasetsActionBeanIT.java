package eionet.cr.web.action;

import eionet.cr.ApplicationTestContext;
import junit.framework.TestCase;
import net.sourceforge.stripes.mock.MockHttpServletResponse;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * @author George Sofianos
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class BrowseDatasetsActionBeanIT {


  /**
   * Tests Basic functionality of browseDatasets action
   * @throws Exception
   */
  @Test
  public void testDefaultEvent() throws Exception {
    MockServletContext ctx = ActionBeanUtils.getServletContext();
    MockRoundtrip trip = new MockRoundtrip(ctx, BrowseDatasetsActionBean.class);
    trip.execute();
    BrowseDatasetsActionBean bean = trip.getActionBean(BrowseDatasetsActionBean.class);

    // http response code = 200
    MockHttpServletResponse response = (MockHttpServletResponse) bean.getContext().getResponse();
    assertEquals(200, response.getStatus());
  }

}
