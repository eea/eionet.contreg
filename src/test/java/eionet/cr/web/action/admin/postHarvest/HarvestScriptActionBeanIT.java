package eionet.cr.web.action.admin.postHarvest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;

import org.apache.commons.lang.time.DateFormatUtils;
import org.junit.Test;

import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestScriptDAO;
import eionet.cr.dto.HarvestScriptDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.web.action.ActionBeanUtils;
import eionet.cr.web.action.admin.harvestscripts.HarvestScriptActionBean;

/**
 * unit test for testing HarvestScriptActionBean.
 *
 * @author kaido
 */
public class HarvestScriptActionBeanIT extends CRDatabaseTestCase {

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
            assertNotNull("Expected a saved script!", savedScript);

            assertEquals(title, savedScript.getTitle());
            assertEquals(HarvestScriptDTO.TargetType.TYPE, savedScript.getTargetType());
            assertEquals("http://targeturl.com", savedScript.getTargetUrl());

            Date lastModified = savedScript.getLastModified();
            assertNotNull("Expected a non-null last-modified-date", lastModified);

            // Note: this may fail if executed right in the second of day-changing, but we'll suffice to that right now.
            assertEquals("Unexpected last-modified date", DateFormatUtils.format(new Date(), "yyyy-MM-dd"),
                    DateFormatUtils.format(lastModified, "yyyy-MM-dd"));
        } finally {
            // Delete inserted script.
            try {
                dao.delete(ids);
            } catch (Exception e) {
                // Ignore deliberately.
            }
        }

        // Test if test source URL is present and correct after redirect.
        if (resolution != null) {
            assertTrue(resolution.getParameters().containsKey("testSourceUrl"));
            Object[] testUrlArr = (Object[]) (resolution.getParameters().get("testSourceUrl"));
            String testUrl = (String) testUrlArr[0];
            assertEquals("http://dummyurl.nowhere.com", testUrl);
        }
    }
}
