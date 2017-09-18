package eionet.cr.web.action.admin.postHarvest;

import java.util.ArrayList;
import java.util.List;

import eionet.cr.ApplicationTestContext;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;

import org.junit.Test;

import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestScriptDAO;
import eionet.cr.dto.HarvestScriptDTO;
import eionet.cr.dto.enums.HarvestScriptType;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.web.action.admin.harvestscripts.HarvestScriptActionBean;
import java.sql.Connection;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * unit test for testing HarvestScriptActionBean.
 *
 * @author kaido
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class HarvestScriptActionBeanIT extends CRDatabaseTestCase {

    @Autowired
    private MockServletContext ctx;

    @Before
    public void setUp() {
        Connection conn = null;
        List<Object> values = new ArrayList<Object>();
        values.add(new Integer(1));
        values.add("url");
        values.add("type");
        values.add("token");
        values.add("admin");
        try {
            conn = SesameUtil.getSQLConnection();
            SQLUtil.executeUpdateReturnAutoID("insert into EXTERNAL_SERVICE (SERVICE_ID, SERVICE_URL, SERVICE_TYPE, SECURE_TOKEN, USER_NAME) values (?, ?, ?, ?, ?)", values, conn);
        } catch (Exception e) {

        } finally {
            SQLUtil.close(conn);
        }
    }

    @After
    public void tearDown() {
        Connection conn = null;
        try {
            conn = SesameUtil.getSQLConnection();
            SQLUtil.execute("DELETE from  EXTERNAL_SERVICE  where SERVICE_URL = '" + "url" + "'", conn);
        } catch (Exception e) {

        } finally {
            SQLUtil.close(conn);
        }
    }

    
    
    /**
     * Test the save action.
     *
     * @throws Exception if testing fails
     */
    @Test
    public void testSave() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(ctx, HarvestScriptActionBean.class);
        String title = "dummyscript_123456";
        trip.setParameter("title", title);
        trip.setParameter("targetType", "TYPE");
        trip.setParameter("targetUrl", "http://targeturl.com");
        trip.setParameter("testSourceUrl", "http://dummyurl.nowhere.com");
        trip.setParameter("type",HarvestScriptType.PUSH.getName());
        trip.setParameter("externalServiceId","1");
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
