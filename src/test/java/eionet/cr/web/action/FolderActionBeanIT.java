package eionet.cr.web.action;

import eionet.acl.AccessController;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.FolderDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.web.action.factsheet.FolderActionBean;
import eionet.cr.web.security.CRUser;
import eionet.cr.web.util.WebConstants;
import net.sourceforge.stripes.mock.MockHttpServletResponse;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;

/**
 * For testing the {@link FolderActionBean}.
 *
 * @author Jaanus
 */
public class FolderActionBeanIT extends CRDatabaseTestCase {

    private static final String USERNAME = "heinlja";
    private static final CRUser USER = new CRUser(USERNAME);
    private static final String PARENT_FOLDER_URI = CRUser.homeUri(USERNAME);
    private static final String SUB_FOLDER_NAME = "test Sub Folder";
    private static final String ACL_PATH = "/home/heinlja/" + SUB_FOLDER_NAME;
    private static final String SUB_FOLDER_NAME_ESCAPED = "test%20Sub%20Folder";
    private static final String EXPECTED_FINAL_FOLDER_URI = PARENT_FOLDER_URI + "/" + SUB_FOLDER_NAME_ESCAPED;

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {

        super.setUp();
        DAOFactory.get().getDao(FolderDAO.class).createUserHomeFolder(USERNAME);
        USER.createDefaultAcls();
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#forceClearTriplesOnSetup()
     */
    @Override
    protected boolean forceClearTriplesOnSetup() {
        return true;
    }

    /**
     *
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    public void testFolderCreation() throws Exception {

        // Assert not-yet-existence of resulting folder subject.
        HelperDAO helperDao = DAOFactory.get().getDao(HelperDAO.class);
        SubjectDTO subject = helperDao.getSubject(EXPECTED_FINAL_FOLDER_URI);
        assertNull("Expected no subject for " + EXPECTED_FINAL_FOLDER_URI, subject);

        // Set up and execute round-trip.
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, FolderActionBean.class);
        trip.getRequest().getSession().setAttribute(WebConstants.USER_SESSION_ATTR, USER);
        trip.setParameter("uri", PARENT_FOLDER_URI);
        trip.setParameter("title", SUB_FOLDER_NAME);
        trip.setParameter("createFolder", StringUtils.EMPTY);
        trip.execute();
        FolderActionBean bean = trip.getActionBean(FolderActionBean.class);

        // Assert response code.
        MockHttpServletResponse response = (MockHttpServletResponse) bean.getContext().getResponse();
        assertEquals(302, response.getStatus());

        // Assert existence of resulting folder subject.
        subject = helperDao.getSubject(EXPECTED_FINAL_FOLDER_URI);
        assertNotNull("Expected a subject for " + EXPECTED_FINAL_FOLDER_URI, subject);

        HashMap acls = AccessController.getAcls();
        assertTrue("Expected at least one ACL", acls != null && !acls.isEmpty());
        assertTrue("Expected an ACL for " + EXPECTED_FINAL_FOLDER_URI, acls.containsKey(ACL_PATH));
    }
}
