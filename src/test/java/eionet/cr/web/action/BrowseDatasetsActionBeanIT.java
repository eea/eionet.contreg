package eionet.cr.web.action;

import junit.framework.TestCase;
import net.sourceforge.stripes.mock.MockHttpServletResponse;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import org.junit.Ignore;

/**
 * @author George Sofianos
 */
@Ignore
public class BrowseDatasetsActionBeanIT extends TestCase {

  public void setUp() throws Exception {
    super.setUp();

  }

  /**
   * Tests Basic functionality of browseDatasets action
   * @throws Exception
   */
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
