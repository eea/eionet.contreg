package eionet.cr.web.action.admin.postHarvest;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;

import org.junit.Test;

import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestScriptDAO;
import eionet.cr.dto.HarvestScriptDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.web.action.ActionBeanUtils;

/**
 * unit test for testing HarvestScriptActionBean.
 *
 * @author kaido
 */
public class HarvestScriptActionBeanTest extends CRDatabaseTestCase {

    /**
     * Test the save action.
     *
     * @throws Exception if testing fails
     */
    @Test
    public void testSave() throws Exception {

        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, HarvestScriptActionBean.class);
        String title = "dummyscript_123456";
        trip.setParameter("title", title);
        trip.setParameter("targetType", "TYPE");
        trip.setParameter("targetUrl", "http://targeturl.com");
        trip.setParameter("testSourceUrl", "http://dummyurl.nowhere.com");

        trip.execute();

        HarvestScriptActionBean bean = trip.getActionBean(HarvestScriptActionBean.class);

        // Save script.
        RedirectResolution resolution = (RedirectResolution) bean.save();

        // Check if script saved correctly.
        int id = bean.getId();
        List<Integer> ids = new ArrayList<Integer>();
        HarvestScriptDAO dao = DAOFactory.get().getDao(HarvestScriptDAO.class);

        try {
            ids.add(id);
            HarvestScriptDTO savedScript = dao.getScriptsByIds(ids).get(0);
            assertEquals(title, savedScript.getTitle());
            assertEquals(HarvestScriptDTO.TargetType.TYPE, savedScript.getTargetType());
            assertEquals("http://targeturl.com", savedScript.getTargetUrl());
        } finally {
            // Delete inserted script.
            dao.delete(ids);
        }
        // Test if test source url is present and correct after redirect.
        if (resolution != null) {
            assertTrue(resolution.getParameters().containsKey("testSourceUrl"));
            Object[] testUrlArr = (Object[]) (resolution.getParameters().get("testSourceUrl"));
            String testUrl = (String) testUrlArr[0];
            assertEquals("http://dummyurl.nowhere.com", testUrl);
        }
    }
}
