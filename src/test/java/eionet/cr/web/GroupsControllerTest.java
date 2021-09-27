package eionet.cr.web;

import eionet.cr.errors.AclLibraryAccessControllerModifiedException;
import eionet.cr.errors.AclPropertiesInitializationException;
import eionet.cr.errors.LdapDaoException;
import eionet.cr.ldap.model.LdapRole;
import eionet.cr.ldap.services.LdapService;
import eionet.cr.util.UserUtil;
import eionet.cr.web.security.CRUser;
import eionet.cr.web.util.WebConstants;
import eionet.cr.web.viewmodel.GroupDetails;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

public class GroupsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private HandleGroupService handleGroupService;

    @Mock
    private LdapService ldapService;

    @Spy
    @InjectMocks
    private GroupsController groupsController;

    private CRUser user;
    private MockHttpSession session;
    private List<LdapRole> ldapRoles;
    private LdapRole ldapRole;
    private LdapRole crLdapRole;
    private Hashtable<String, Vector<String>> groupsAndUsers;
    private ArrayList<String> roles;
    private GroupDetails groupDetails;
    private static final String ACL_GROUP = "cr_admin";
    private static final String TEST_USER = "testUser";
    private static final String TEST_ROLE = "testRole";
    private static final String LDAP_CR_ADMIN = "ldap-cr-admin";

    @Before
    public void setUp() throws LdapDaoException, AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        MockitoAnnotations.openMocks(this);
        user = mock(CRUser.class);
        setSession();
        setLdapRoles();
        setGroupsAndUsers();
        setRoleNames();
        setGroupDetails();
        UserUtil.setCrGroupsAndUsers(groupsAndUsers);
        when(ldapService.getUserLdapRoles(anyString())).thenReturn(ldapRoles);
        when(ldapService.getAllLdapRoles()).thenReturn(ldapRoles);
        when(user.hasPermission(anyString(), anyString())).thenReturn(true);
        when(user.isCrAdmin()).thenReturn(true);
        Mockito.doNothing().when(groupsController).refreshUserGroupResults(any(HttpServletRequest.class));
        mockMvc = MockMvcBuilders.standaloneSetup(groupsController).build();
    }

    private void setGroupDetails() {
        groupDetails = new GroupDetails();
        groupDetails.setLdapGroupName("testRole");
    }

    private void setRoleNames() {
        roles = new ArrayList<>();
        roles.add(TEST_ROLE);
        roles.add(LDAP_CR_ADMIN);
    }

    void setGroupsAndUsers() {
        groupsAndUsers = new Hashtable<>();
        Vector<String> vector = new Vector<>();
        vector.add(TEST_USER);
        groupsAndUsers.put(ACL_GROUP, vector);
    }

    void setSession() {
        session = new MockHttpSession();
        session.setAttribute(WebConstants.USER_SESSION_ATTR, user);
    }

    void setLdapRoles() {
        ldapRoles = new ArrayList<>();
        ldapRole = new LdapRole();
        crLdapRole = new LdapRole();
        ldapRole.setName(TEST_ROLE);
        crLdapRole.setName(LDAP_CR_ADMIN);
        ldapRoles.add(ldapRole);
        ldapRoles.add(crLdapRole);
    }

    @Test
    public void testGetGroupsAndUsersSuccess() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/admintools/list")
                .session(session);
        mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(view().name("groupsAndUsers"));
    }

    @Test
    public void testGetGroupsAndUsersUserNotLoggedIn() throws Exception {
        when(user.hasPermission(anyString(), anyString())).thenReturn(false);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/admintools/list");
        mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(view().name("message"));
    }

    @Test
    public void testGetGroupsAndUsersNoPermission() throws Exception {
        when(user.hasPermission(anyString(), anyString())).thenReturn(false);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/admintools/list")
                .session(session);
        mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(view().name("message"));
    }

    @Test
    public void testGetAllLdapRolesSuccess() throws LdapDaoException {
        List<String> result = groupsController.getAllLdapRoles();
        assertEquals(roles, result);
    }

    @Test
    public void testAddUserSuccess() throws Exception {
        when(ldapService.getAllLdapRoles()).thenReturn(ldapRoles);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/admintools/addUser")
                .session(session).flashAttr("groupDetails", groupDetails);
        mockMvc.perform(builder)
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void testAddUserNoPermission() throws Exception {
        when(user.hasPermission(anyString(), anyString())).thenReturn(false);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/admintools/addUser")
                .session(session).flashAttr("groupDetails", groupDetails);
        mockMvc.perform(builder)
                .andExpect(view().name("message"));
    }

    @Test
    public void testAddUserGroupNotExist() throws Exception {
        groupDetails.setLdapGroupName("test");
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/admintools/addUser")
                .session(session).flashAttr("groupDetails", groupDetails);
        mockMvc.perform(builder)
                .andExpect(view().name("message"));
    }

    @Test
    public void testRemoveUserSuccess() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/admintools/removeUser")
                .session(session).param("crGroupName", ACL_GROUP).param("memberName", "test");
        mockMvc.perform(builder)
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void testRemoveUserNoPermission() throws Exception {
        when(user.hasPermission(anyString(), anyString())).thenReturn(false);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/admintools/removeUser")
                .session(session).param("crGroupName", ACL_GROUP).param("memberName", "test");
        mockMvc.perform(builder)
                .andExpect(view().name("message"));
    }
}
